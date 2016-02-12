package es.icarto.gvsig.extgex;

import es.icarto.gvsig.commons.AbstractExtension;
import es.icarto.gvsig.extgex.queries.QueriesPanel;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class QueriesExtension extends AbstractExtension {

    @Override
    public void execute(String actionCommand) {
	QueriesPanel validationPanel = new QueriesPanel();
	validationPanel.openDialog();
    }

    @Override
    public boolean isEnabled() {
	return DBSession.isActive();
    }

    @Override
    public void initialize() {
	// nothing to do here
	// we only use icons when there is a button in the toolbar
    }

}
