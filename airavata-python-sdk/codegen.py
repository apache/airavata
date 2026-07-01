#!/usr/bin/env python3
# /// script
# requires-python = ">=3.10"
# dependencies = [
#     "grpcio-tools==1.80.0",
#     "googleapis-common-protos>=1.62.0",
# ]
# ///
"""Regenerate airavata/model + airavata/services from the airavata-api protos.

Run: `uv run --script codegen.py` (needs an airavata-api sibling checkout).
"""

from __future__ import annotations

import os
import shutil
import sys
import tempfile
from pathlib import Path

SDK_ROOT = Path(__file__).resolve().parent
PROTO_API_ROOT = SDK_ROOT.parent / "airavata-api"
MODEL_PROTO_ROOT = PROTO_API_ROOT / "src" / "main" / "proto"

AIRAVATA_PKG = SDK_ROOT / "airavata"
MODEL_OUT = AIRAVATA_PKG / "model"
SERVICES_OUT = AIRAVATA_PKG / "services"

SMOKE_CHECKS = [
    (SERVICES_OUT / "experiment_service_pb2_grpc.py", "GetFullExperiment"),
    (SERVICES_OUT / "experiment_set_service_pb2_grpc.py", "ExperimentSetServiceStub"),
    (MODEL_OUT / "commons" / "commons_pb2.pyi", "AccessFlags"),
]


def _fail(msg: str) -> None:
    sys.stderr.write(f"codegen: {msg}\n")
    sys.exit(1)


def _underscore(name: str) -> str:
    return name.replace("-", "_")


def _service_protos() -> list[Path]:
    protos = [
        p
        for p in PROTO_API_ROOT.glob("*/src/main/proto/*.proto")
        if "/org/" not in p.as_posix()
    ]
    if not protos:
        _fail(f"no service protos found under {PROTO_API_ROOT}/*/src/main/proto/")
    return sorted(protos)


def _rewrite_proto_imports(text: str) -> str:
    # flat sibling-service import -> services/<underscored>.proto
    out = []
    for line in text.splitlines(keepends=True):
        stripped = line.strip()
        if stripped.startswith("import \"") and "/" not in stripped[len("import \""):]:
            inner = stripped[len("import \"") : stripped.index("\";", len("import \""))]
            out.append(f'import "services/{_underscore(inner)}";\n')
        else:
            out.append(line)
    return "".join(out)


def _rewrite_py(text: str) -> str:
    # rewrite only the import lines + the BuildTop module-name; never the
    # serialized descriptors (they carry the org.apache.airavata.* wire identity).
    out = []
    for line in text.splitlines(keepends=True):
        if line.startswith("from org.apache.airavata."):
            line = "from airavata." + line[len("from org.apache.airavata."):]
        elif line.startswith("from services import") or line.startswith("from services."):
            line = "from airavata." + line[len("from "):]
        elif "BuildTopDescriptorsAndMessages(DESCRIPTOR, 'org.apache.airavata." in line:
            line = line.replace(
                "BuildTopDescriptorsAndMessages(DESCRIPTOR, 'org.apache.airavata.",
                "BuildTopDescriptorsAndMessages(DESCRIPTOR, 'airavata.",
            )
        out.append(line)
    return "".join(out)


def _relocate_and_rewrite(gen_dir: Path) -> None:
    for d in (MODEL_OUT, SERVICES_OUT):
        if d.exists():
            shutil.rmtree(d)

    model_src = gen_dir / "org" / "apache" / "airavata" / "model"
    if not model_src.is_dir():
        _fail(f"protoc produced no model tree at {model_src}")
    services_src = gen_dir / "services"
    if not services_src.is_dir():
        _fail(f"protoc produced no services tree at {services_src}")

    def _copy_tree(src: Path, dst: Path) -> None:
        for f in sorted(src.rglob("*")):
            if f.is_dir():
                continue
            dest = dst / f.relative_to(src)
            dest.parent.mkdir(parents=True, exist_ok=True)
            if f.suffix in (".py", ".pyi"):
                dest.write_text(_rewrite_py(f.read_text()))
            else:
                shutil.copy2(f, dest)

    _copy_tree(model_src, MODEL_OUT)
    _copy_tree(services_src, SERVICES_OUT)

    for root in (MODEL_OUT, SERVICES_OUT):
        for d in [root, *[p for p in root.rglob("*") if p.is_dir()]]:
            init = d / "__init__.py"
            if not init.exists():
                init.write_text("")


def _proto_includes() -> list[str]:
    import grpc_tools

    includes = []
    builtin = Path(grpc_tools.__file__).resolve().parent / "_proto"
    if builtin.is_dir():
        includes.append(str(builtin))
    for entry in sys.path:
        cand = Path(entry) / "google" / "api" / "annotations.proto"
        if cand.is_file():
            includes.append(entry)
            break
    else:
        _fail("google/api/annotations.proto not found; install googleapis-common-protos")
    return includes


def _stage(stage: Path) -> None:
    src_org = MODEL_PROTO_ROOT / "org"
    if not src_org.is_dir():
        _fail(f"model proto root not found: {src_org}")
    shutil.copytree(src_org, stage / "org")

    services_dir = stage / "services"
    services_dir.mkdir(parents=True)
    for proto in _service_protos():
        dest = services_dir / _underscore(proto.name)
        dest.write_text(_rewrite_proto_imports(proto.read_text()))


def _run_protoc(stage: Path, out_dir: Path, rel_protos: list[str], includes: list[str]) -> None:
    from grpc_tools import protoc

    args = [
        "grpc_tools.protoc",
        f"--proto_path={stage}",
        *[f"--proto_path={inc}" for inc in includes],
        f"--python_out={out_dir}",
        f"--grpc_python_out={out_dir}",
        f"--pyi_out={out_dir}",
        *rel_protos,
    ]
    prev = os.getcwd()
    os.chdir(stage)
    try:
        rc = protoc.main(args)
    finally:
        os.chdir(prev)
    if rc != 0:
        _fail(f"protoc exited {rc} for: {' '.join(rel_protos[:3])}...")


def main() -> None:
    if not PROTO_API_ROOT.is_dir():
        _fail(f"airavata-api not found at {PROTO_API_ROOT} (sibling checkout required)")
    AIRAVATA_PKG.mkdir(parents=True, exist_ok=True)

    with tempfile.TemporaryDirectory(prefix="airavata-codegen-") as tmp:
        stage = Path(tmp) / "stage"
        gen = Path(tmp) / "gen"
        gen.mkdir(parents=True)
        _stage(stage)
        includes = _proto_includes()

        org_protos = [
            str(p.relative_to(stage)) for p in sorted((stage / "org").rglob("*.proto"))
        ]
        svc_protos = [
            str(p.relative_to(stage))
            for p in sorted((stage / "services").glob("*.proto"))
        ]
        print(f"codegen: {len(org_protos)} model protos, {len(svc_protos)} service protos")
        # two passes (models, then services); a combined run fails protoc path relativization
        _run_protoc(stage, gen, org_protos, includes)
        _run_protoc(stage, gen, svc_protos, includes)

        _relocate_and_rewrite(gen)

    failures = [
        f"{path.relative_to(SDK_ROOT)} missing '{token}'"
        for path, token in SMOKE_CHECKS
        if not (path.is_file() and token in path.read_text())
    ]
    if failures:
        _fail("smoke check failed: " + "; ".join(failures))
    print(f"codegen: regenerated {MODEL_OUT.relative_to(SDK_ROOT)} + {SERVICES_OUT.relative_to(SDK_ROOT)}")


if __name__ == "__main__":
    main()
