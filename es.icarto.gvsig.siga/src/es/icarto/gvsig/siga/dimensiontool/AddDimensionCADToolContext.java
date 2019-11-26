package es.icarto.gvsig.siga.dimensiontool;

import java.awt.event.InputEvent;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.gui.cad.CADStatus;

public class AddDimensionCADToolContext extends statemap.FSMContext {

    // ---------------------------------------------------------------
    // Member methods.
    //
    public AddDimensionCADToolContext(AddDimensionCADTool owner) {
        super();

        _owner = owner;
        setState(Dimension.FirstPoint);
        Dimension.FirstPoint.Entry(this);
    }

    public void addOption(String s) {
        _transition = "addOption";
        getState().addOption(this, s);
        _transition = "";
        return;
    }

    public void addPoint(double pointX, double pointY, InputEvent event) {
        _transition = "addPoint";
        getState().addPoint(this, pointX, pointY, event);
        _transition = "";
        return;
    }

    public void addValue(double d) {
        _transition = "addValue";
        getState().addValue(this, d);
        _transition = "";
        return;
    }

    public AddDimensionCADToolState getState()
            throws statemap.StateUndefinedException {
        if (_state == null) {
            throw (new statemap.StateUndefinedException());
        }

        return ((AddDimensionCADToolState) _state);
    }

    protected AddDimensionCADTool getOwner() {
        return (_owner);
    }

    public void removePoint(InputEvent event, int numPoints) {
        _transition = "removePoint";
        getState().removePoint(this, event, numPoints);
        _transition = "";
        return;
    }

    // ---------------------------------------------------------------
    // Member data.
    //

    transient private AddDimensionCADTool _owner;

    // ---------------------------------------------------------------
    // Inner classes.
    //

    public static abstract class AddDimensionCADToolState extends
            statemap.State {
        // -----------------------------------------------------------
        // Member methods.
        //

        protected AddDimensionCADToolState(String name, int id) {
            super(name, id);
        }

        protected void Entry(AddDimensionCADToolContext context) {
        }

        protected void Exit(AddDimensionCADToolContext context) {
        }

        protected abstract String[] getDescription();

        protected void addOption(AddDimensionCADToolContext context, String s) {
            Default(context);
        }

        protected void addPoint(AddDimensionCADToolContext context,
                double pointX, double pointY, InputEvent event) {
            Default(context);
        }

        protected void addValue(AddDimensionCADToolContext context, double d) {
            Default(context);
        }

        protected void Default(AddDimensionCADToolContext context) {
            throw (new statemap.TransitionUndefinedException("State: "
                    + context.getState().getName() + ", Transition: "
                    + context.getTransition()));
        }

        protected void removePoint(AddDimensionCADToolContext context,
                InputEvent event, int numPoints) {
            Default(context);
        }
        // -----------------------------------------------------------
        // Member data.
        //
    }

    /* package */static abstract class Dimension {
        // -----------------------------------------------------------
        // Member methods.
        //

        // -----------------------------------------------------------
        // Member data.
        //

        // -------------------------------------------------------
        // Statics.
        //
        /* package */static Dimension_Default.Dimension_FirstPoint FirstPoint;
        /* package */static Dimension_Default.Dimension_SecondPoint SecondPoint;
        private static Dimension_Default Default;

        static {
            FirstPoint = new Dimension_Default.Dimension_FirstPoint(
                    "AddDimension.FirstPoint", 0);
            SecondPoint = new Dimension_Default.Dimension_SecondPoint(
                    "AddDimension.SecondPoint", 1);
            Default = new Dimension_Default("AddDimension.Default", -1);
        }

    }

    protected static class Dimension_Default extends AddDimensionCADToolState {
        // -----------------------------------------------------------
        // Member methods.
        //

        protected Dimension_Default(String name, int id) {
            super(name, id);
        }

        @Override
        protected String[] getDescription() {
            return new String[] { "cancel" };
        }

        @Override
        protected void addOption(AddDimensionCADToolContext context, String s) {
            AddDimensionCADTool ctxt = context.getOwner();

            if (s.equals(PluginServices.getText(this, "cancel"))) {
                if (context.getState().getName()
                        .equals(Dimension.FirstPoint.getName())) {
                    context.getOwner().end();
                    return;
                }

                (context.getState()).Exit(context);

                context.clearState();
                try {
                    ctxt.cancel();
                } finally {
                    context.setState(Dimension.FirstPoint);
                    (context.getState()).Entry(context);

                }
            } else if (s.equals("")) {
                boolean loopbackFlag = context.getState().getName()
                        .equals(Dimension.FirstPoint.getName());

                if (loopbackFlag == false) {
                    (context.getState()).Exit(context);
                }

                context.clearState();
                try {
                    ctxt.endGeometry();
                } finally {
                    context.setState(Dimension.FirstPoint);

                    if (loopbackFlag == false) {
                        (context.getState()).Entry(context);
                    }

                }
            } else {
                ctxt.throwOptionException(
                        PluginServices.getText(this, "incorrect_option"), s);
            }

            return;
        }

        @Override
        protected void addValue(AddDimensionCADToolContext context, double d) {
            AddDimensionCADTool ctxt = context.getOwner();

            boolean loopbackFlag = context.getState().getName()
                    .equals(Dimension.FirstPoint.getName());

            if (loopbackFlag == false) {
                (context.getState()).Exit(context);
            }

            context.clearState();
            try {
                ctxt.throwValueException(
                        PluginServices.getText(this, "incorrect_value"), d);
            } finally {
                context.setState(Dimension.FirstPoint);

                if (loopbackFlag == false) {
                    (context.getState()).Entry(context);
                }

            }
            return;
        }

        @Override
        protected void addPoint(AddDimensionCADToolContext context,
                double pointX, double pointY, InputEvent event) {
            AddDimensionCADTool ctxt = context.getOwner();

            boolean loopbackFlag = context.getState().getName()
                    .equals(Dimension.FirstPoint.getName());

            if (loopbackFlag == false) {
                (context.getState()).Exit(context);
            }

            context.clearState();
            try {
                ctxt.throwPointException(
                        PluginServices.getText(this, "incorrect_point"),
                        pointX, pointY);
            } finally {
                context.setState(Dimension.FirstPoint);

                if (loopbackFlag == false) {
                    (context.getState()).Entry(context);
                }

            }
            return;
        }

        @Override
        protected void removePoint(AddDimensionCADToolContext context,
                InputEvent event, int numPoints) {
            AddDimensionCADTool ctxt = context.getOwner();

            boolean loopbackFlag = context.getState().getName()
                    .equals(Dimension.FirstPoint.getName());

            if (loopbackFlag == false) {
                (context.getState()).Exit(context);
            }

            context.clearState();
            try {
                ctxt.throwNoPointsException(PluginServices.getText(this,
                        "no_points"));
            } finally {
                context.setState(Dimension.FirstPoint);

                if (loopbackFlag == false) {
                    (context.getState()).Entry(context);
                }

            }
            return;
        }

        // -----------------------------------------------------------
        // Inner classse.
        //

        private static final class Dimension_FirstPoint extends Dimension_Default {
            // -------------------------------------------------------
            // Member methods.
            //

            private Dimension_FirstPoint(String name, int id) {
                super(name, id);
            }

            @Override
            protected String[] getDescription() {
                return new String[] { "cancel" };
            }

            @Override
            protected void Entry(AddDimensionCADToolContext context) {
                AddDimensionCADTool ctxt = context.getOwner();

                ctxt.setQuestion(PluginServices.getText(this,
                        "insert_first_point"));
                ctxt.setDescription(getDescription());
                return;
            }

            @Override
            protected void addPoint(AddDimensionCADToolContext context,
                    double pointX, double pointY, InputEvent event) {
                AddDimensionCADTool ctxt = context.getOwner();

                (context.getState()).Exit(context);
                context.clearState();
                try {
                    ctxt.addPoint(pointX, pointY, event);
                } finally {
                    context.setState(Dimension.SecondPoint);
                    (context.getState()).Entry(context);
                }
                return;
            }

            // -------------------------------------------------------
            // Member data.
            //
        }

        private static final class Dimension_SecondPoint extends
                Dimension_Default {
            // -------------------------------------------------------
            // Member methods.
            //

            private Dimension_SecondPoint(String name, int id) {
                super(name, id);
            }

            @Override
            protected String[] getDescription() {
                return new String[] { "cancel", "removePoint" };
            }

            @Override
            protected void Entry(AddDimensionCADToolContext context) {
                AddDimensionCADTool ctxt = context.getOwner();
                boolean deleteButton3 = CADStatus.getCADStatus()
                        .isDeleteButtonActivated();
                if (deleteButton3) {
                    ctxt.setQuestion(PluginServices.getText(this,
                            "insert_second_point_del"));
                } else {
                    ctxt.setQuestion(PluginServices.getText(this,
                            "insert_second_point"));
                }
                ctxt.setDescription(getDescription());
                return;
            }

            @Override
            protected void addPoint(AddDimensionCADToolContext context,
                    double pointX, double pointY, InputEvent event) {
                AddDimensionCADTool ctxt = context.getOwner();

                context.clearState();

                ctxt.setQuestion(null);
                ctxt.addPoint(pointX, pointY, event);
                ctxt.endGeometry();
                ctxt.end();
                return;
            }

            @Override
            protected void removePoint(AddDimensionCADToolContext context,
                    InputEvent event, int numPoints) {
                AddDimensionCADTool ctxt = context.getOwner();

                if (numPoints > 0) {

                    (context.getState()).Exit(context);
                    context.clearState();
                    try {
                        ctxt.removePoint(event);

                    } finally {
                        context.setState(Dimension.FirstPoint);
                        (context.getState()).Entry(context);
                    }
                } else {
                    super.removePoint(context, event, numPoints);
                }

                return;
            }

            // -------------------------------------------------------
            // Member data.
            //
        }

        // -----------------------------------------------------------
        // Member data.
        //
    }
}
