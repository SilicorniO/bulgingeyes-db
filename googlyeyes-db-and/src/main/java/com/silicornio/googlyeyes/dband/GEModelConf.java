package com.silicornio.googlyeyes.dband;

public class GEModelConf {

	/** Configuration of the model **/
	protected GEModelConfiguration configuration;
	
	/** List of objects with the model **/
	protected GEModelObject[] objects;

	public GEModelConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(GEModelConfiguration configuration) {
		this.configuration = configuration;
	}

	public GEModelObject[] getObjects() {
		return objects;
	}

	public void setObjects(GEModelObject[] objects) {
		this.objects = objects;
	}
}
