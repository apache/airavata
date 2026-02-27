BASEDIR:=$(abspath $(dir $(lastword $(MAKEFILE_LIST)))/../..)
OUTDIR:=$(abspath $(dir $(lastword $(MAKEFILE_LIST))))

clean:
	rm -rf $(OUTDIR)/dist
	rm -rf $(OUTDIR)/build
	rm -rf $(OUTDIR)/airavata.egg-info

build: clean
	python -m build

deploy: build
	twine upload --repository airavata dist/*
