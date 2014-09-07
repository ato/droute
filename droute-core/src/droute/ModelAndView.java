package droute;

public class ModelAndView {
	private final Object model; 
	private final String view;

	public ModelAndView(Object model, String view) {
		this.model = model;
		this.view = view;
	}
	
	public String view() {
		return view;
	}

	public Object model() {
		return model;
	}

}
