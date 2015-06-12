package org.apache.airavata.gfac.impl;

import org.apache.airavata.gfac.core.GFac;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.context.ProcessContext;

public class GFacImpl implements GFac {

	@Override
	public boolean submitProcess(ProcessContext processContext) throws GFacException {
		return false;
	}

	@Override
	public void invokeProcessOutFlow(ProcessContext processContext) throws GFacException {

	}

	@Override
	public void reInvokeProcessOutFlow(ProcessContext processContext) throws GFacException {

	}

	@Override
	public boolean cancelProcess(ProcessContext processContext) throws GFacException {
		return false;
	}
}
