package es.icarto.gvsig.siga;

public class ZoomExtension extends AbstractExtension {

    @Override
    public void execute(String actionCommand) {
	System.out.println("It works!");
    }

    @Override
    public boolean isEnabled() {
	return getView() != null;
    }

}
