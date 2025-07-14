BASEDIR:=$(abspath $(dir $(lastword $(MAKEFILE_LIST)))/../..)
OUTDIR:=$(abspath $(dir $(lastword $(MAKEFILE_LIST))))

clean:
	rm -rf $(OUTDIR)/airavata
	rm -rf $(OUTDIR)/dist
	rm -rf $(OUTDIR)/build
	rm -rf $(OUTDIR)/airavata.egg-info

thrift: clean
	thrift -r -o $(OUTDIR) --gen py:enum,type_hints ${BASEDIR}/thrift-interface-descriptions/stubs_python.thrift && \
	cp -r $(OUTDIR)/gen-py/airavata $(OUTDIR) && \
	rm -rf $(OUTDIR)/gen-py

build: thrift
	python -m build

deploy: build
	twine upload --repository airavata dist/*