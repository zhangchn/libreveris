//-----------------------------------------------------------------------//
//                                                                       //
//                                Z o o m                                //
//                                                                       //
//  Copyright (C) Herve Bitteur 2000-2005. All rights reserved.          //
//  This software is released under the terms of the GNU General Public  //
//  License. Please contact the author at herve.bitteur@laposte.net      //
//  to report bugs & suggestions.                                        //
//-----------------------------------------------------------------------//

package omr.ui;

import omr.constant.Constant;
import omr.constant.ConstantSet;
import omr.util.Logger;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Set;

import javax.swing.event.*;

/**
 * Class <code>Zoom</code> encapsulates a zoom ratio, with methods to go
 * between model/source values (coordinates, lengths) and display values.
 *
 * <p>A {@link LogSlider} can be connected to this zoom entity, to provide
 * UI for both output and input.
 *
 * <p>Stolen from Swing implementation, it also handles a list of change
 * listeners.  </p>
 *
 * @author Herv&eacute Bitteur
 * @version $Id$
 */
public class Zoom
{
    //~ Static variables/initializers -------------------------------------

    private static final Logger    logger    = Logger.getLogger(Zoom.class);
    private static final Constants constants = new Constants();

    // To assign a unique Id
    private static int globalId;

    //~ Instance variables ------------------------------------------------

    // Unique Id
    private int id = ++globalId;

    /** Current ratio value */
    protected double ratio;

    /** Potential logarithmic slider to drive this zoom */
    protected LogSlider slider;

    /** List of event listeners */
    protected Set<ChangeListener> listeners = new HashSet<ChangeListener>();

    /** Unique event, created lazily */
    protected transient ChangeEvent changeEvent = null;

    //~ Constructors ------------------------------------------------------

    //------//
    // Zoom //
    //------//
    /**
     * Create a zoom entity, with a default ratio value of 1.
     */
    public Zoom ()
    {
        this(1);
    }

    //------//
    // Zoom //
    //------//
    /**
     * Create a zoom entity, with the provided initial ratio value.
     *
     * @param ratio the initial ratio value
     */
    public Zoom (double ratio)
    {
        if (logger.isDebugEnabled()) {
            logger.debug("Zoom created with ratio " + ratio);
        }

        setRatio(ratio);
    }

    //------//
    // Zoom //
    //------//

    /**
     * Create a zoom entity, with the provided initial ratio value. and a
     * related slider
     *
     * @param slider the related slider
     * @param ratio the initial ratio value
     */
    public Zoom (LogSlider slider,
                 double    ratio)
    {
        if (logger.isDebugEnabled()) {
            logger.debug("Zoom created" +
                         " slider=" + slider +
                         " ratio=" + ratio);
        }

        setSlider(slider);
        setRatio(ratio);
    }

    //~ Methods -----------------------------------------------------------

    //----------//
    // setRatio //
    //----------//
    /**
     * Change the display zoom ratio. Nota, if the zoom is coupled with a
     * slider, this slider has the final word concerning the precise zoom
     * value, since the slider uses integer (or fractional) values.
     *
     * @param ratio the new ratio
     */
    public void setRatio (double ratio)
    {
        if (logger.isDebugEnabled()) {
            logger.debug("setRatio ratio=" + ratio);
        }

        // Propagate to slider (useful to keep slider in sync when ratio is
        // set programmatically)
        if (slider != null) {
            slider.setDoubleValue(ratio);
        } else {
            forceRatio(ratio);
        }
    }

    //------------//
    // forceRatio //
    //------------//
    /**
     * Impose the new ratio (should be called by the slider only)
     *
     * @param ratio the new ratio
     */
    public void forceRatio (double ratio)
    {
        if (logger.isDebugEnabled()) {
            logger.debug("forceRatio ratio=" + ratio);
        }

        this.ratio = ratio;

        // Propagate to listeners
        fireStateChanged();
    }

    //----------//
    // getRatio //
    //----------//
    /**
     * Return the current zoom ratio. Ratio is defined as display / source.
     *
     * @return the ratio
     */
    public double getRatio ()
    {
        return ratio;
    }

    //-----------//
    // setSlider //
    //-----------//
    /**
     * Define a related logarithmic slider, as a UI to adjust the zoom
     * value
     *
     * @param slider the related slider UI
     */
    public void setSlider (final LogSlider slider)
    {
        this.slider = slider;

        if (logger.isDebugEnabled()) {
            logger.debug("setSlider");
        }

        if (slider != null) {
            slider.setDoubleValue(ratio);

            slider.addChangeListener(new ChangeListener() {
                    public void stateChanged (ChangeEvent e)
                    {
                        // Forward the new zoom ratio
                        if (constants.continuousSliderReading.getValue()
                            || ! slider.getValueIsAdjusting()) {
                            double newRatio = slider.getDoubleValue();

                            if (logger.isDebugEnabled()) {
                                logger.debug("Slider firing zoom newRatio=" + newRatio);
                            }

                            // Stop condition to avoid endless loop between
                            // slider and zoom
                            if (newRatio != ratio) {
                                forceRatio(newRatio);
                            }
                        }
                    }
                });
        }
    }

    //-------------------//
    // addChangeListener //
    //-------------------//
    /**
     * Register a change listener, to be notified when the zoom value is
     * changed
     *
     * @param listener the listener to be notified
     */
    public void addChangeListener (ChangeListener listener)
    {
        listeners.add(listener);

        if (logger.isDebugEnabled()) {
            logger.debug("addChangeListener " + listener +
                         " -> " + listeners.size());
        }
    }

    //----------------------//
    // removeChangeListener //
    //----------------------//
    /**
     * Unregister a change listener
     *
     * @param listener the listener to remove
     */
    public void removeChangeListener (ChangeListener listener)
    {
        listeners.remove(listener);
    }

    //-------//
    // scale //
    //-------//
    /**
     * Scale all the elements of provided point
     *
     * @param pt the point to be scaled
     */
    public void scale (Point pt)
    {
        pt.x = scaled(pt.x);
        pt.y = scaled(pt.y);
    }

    //-------//
    // scale //
    //-------//
    /**
     * Scale all the elements of provided dimension
     *
     * @param dim the dimension to be scaled
     */
    public void scale (Dimension dim)
    {
        dim.width  = scaled(dim.width);
        dim.height = scaled(dim.height);
    }

    //-------//
    // scale //
    //-------//
    /**
     * Scale all the elements of provided rectangle
     *
     * @param rect the rectangle to be scaled
     */
    public void scale (Rectangle rect)
    {
        rect.x      = scaled(rect.x);
        rect.y      = scaled(rect.y);
        rect.width  = scaled(rect.width);
        rect.height = scaled(rect.height);
    }

    //--------//
    // scaled //
    //--------//
    /**
     * Coordinate computation, Source -> Display
     *
     * @param val a source value
     *
     * @return the (scaled) display value
     */
    public int scaled (double val)
    {
        return (int) Math.rint(val * ratio);
    }

    //--------//
    // scaled //
    //--------//
    /**
     * Coordinate computation, Source -> Display
     *
     * @param dim source dimension
     *
     * @return the corresponding (scaled) dimension
     */
    public Dimension scaled (Dimension dim)
    {
        Dimension d = new Dimension(dim);
        scale(d);

        return d;
    }

    //--------//
    // scaled //
    //--------//
    /**
     * Coordinate computation, Source -> Display
     *
     * @param rect source rectangle
     *
     * @return the corresponding (scaled) rectangle
     */
    public Rectangle scaled (Rectangle rect)
    {
        Rectangle r = new Rectangle(rect);
        scale(r);

        return r;
    }

    //-------------//
    // truncScaled //
    //-------------//
    /**
     * Coordinate computation, Source -> Display, but with a truncation
     * rather than rounding.
     *
     * @param val a source value
     *
     * @return the (scaled) display value
     */
    public int truncScaled (double val)
    {
        return (int) (val * ratio);
    }

    //---------------//
    // truncUnscaled //
    //---------------//
    /**
     * Coordinate computation, Display -> Source, but with a truncation
     * rather than rounding.
     *
     * @param val a display value
     *
     * @return the corresponding (unscaled) source coordinate
     */
    public int truncUnscaled (double val)
    {
        return (int) (val / ratio);
    }

    //----------//
    // unscaled //
    //----------//
    /**
     * Coordinate computation Display -> Source
     *
     * @param val a display value
     *
     * @return the corresponding (unscaled) source coordinate
     */
    public int unscaled (double val)
    {
        return (int) Math.rint(val / ratio);
    }

    //------------------//
    // fireStateChanged //
    //------------------//
    /**
     * In charge of forwarding the change notification to all registered
     * listeners
     */
    protected void fireStateChanged ()
    {
        if (logger.isDebugEnabled()) {
            logger.debug("Listeners= " + listeners.size());

            for (ChangeListener listener : listeners) {
                logger.debug(this + " will Fire " + listener);
            }
        }

        for (ChangeListener listener : listeners) {
            if (changeEvent == null) {
                changeEvent = new ChangeEvent(this);
            }
            if (logger.isDebugEnabled()) {
                logger.debug(this + " Firing " + listener);
            }

            listener.stateChanged(changeEvent);
        }
    }

    //----------//
    // toString //
    //----------//
    /**
     * Report a quick description
     *
     * @return the zoom information
     */
    @Override
        public String toString()
    {
        return "{Zoom#" + id + " listeners=" + listeners.size() +
            " ratio=" + ratio + "}";
    }

    //~ Classes -----------------------------------------------------------

    //-----------//
    // Constants //
    //-----------//
    private static class Constants
        extends ConstantSet
    {
        Constant.Boolean continuousSliderReading
            = new Constant.Boolean
            (true,
             "Should we allow continuous reading of the zoom slider");

        Constants ()
        {
            initialize();
        }
    }
}
