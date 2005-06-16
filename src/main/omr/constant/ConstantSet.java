//-----------------------------------------------------------------------//
//                                                                       //
//                         C o n s t a n t S e t                         //
//                                                                       //
//  Copyright (C) Herve Bitteur 2000-2005. All rights reserved.          //
//  This software is released under the terms of the GNU General Public  //
//  License. Please contact the author at herve.bitteur@laposte.net      //
//  to report bugs & suggestions.                                        //
//-----------------------------------------------------------------------//

package omr.constant;

import omr.util.Logger;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This abstract class handles a set of Constants as a whole. In
 * particular, this allows a user interface (such as {@link UnitTreeTable})
 * to present an editing table of the whole set of constants.
 *
 * <p>We recommend to define only one such static ConstantSet per
 * class/unit as a subclass of this (abstract) ConstantSet. </p>
 *
 * @author Herv&eacute Bitteur
 * @version $Id$
 */
public abstract class ConstantSet
{
    //~ Static variables/initializers -------------------------------------

    private static final Logger logger = Logger.getLogger(ConstantSet.class);

    //~ Instance variables ------------------------------------------------

    // The containing unit/class
    private final String unit;

    // The mapping between constant name & constant object. We use a sorted
    // map to allow access by constant index in constant set, as required
    // by ConstantTreeTable.
    private final SortedMap<String, Constant> map = new TreeMap<String, Constant>();

    //~ Constructors ------------------------------------------------------

    //-------------//
    // ConstantSet //
    //-------------//

    /**
     * Registers this ConstantSet at ConstantSetMAnager
     */
    public ConstantSet ()
    {
        this.unit = getClass().getDeclaringClass().getName();
        UnitManager.getInstance().addSet(this);
    }

    //~ Methods -----------------------------------------------------------

    //------------//
    // isModified //
    //------------//

    /**
     * Predicate to check whether at least one of the constant of the set
     * has been modified
     *
     * @return the modification status of the whole set
     */
    public boolean isModified ()
    {
        for (Constant constant : map.values()) {
            if (constant.isModified()) {
                return true;
            }
        }

        return false;
    }

    //------//
    // dump //
    //------//

    /**
     * A utility method to dump current value of each constant in the set.
     */
    public void dump ()
    {
        System.out.println("\n[" + unit + "]");
        for (Constant constant : map.values()) {
            System.out.printf("%-25s = %s\n", constant.getName(),
                              constant.currentString());
        }
    }

    //----------//
    // toString //
    //----------//

    /**
     * Return the last part of the ConstantSet name, without the leading
     * package names. This short name is used by Constant TreeTable
     *
     * @return just the (unqualified) name of the ConstantSet
     */
    public String toString ()
    {
        StringBuffer sb = new StringBuffer();

        //sb.append("<html><b>");
        int dot = unit.lastIndexOf('.');

        if (dot != -1) {
            sb.append(unit.substring(dot + 1));
        } else {
            sb.append(unit);
        }

        //sb.append("</b></html>");
        return sb.toString();
    }

    //------------//
    // initialize //
    //------------//

    /**
     * This is meant to be called in the constructor of subclasses, to
     * actually initialize some needed fields in the ConstantSet.
     */
    protected void initialize ()
    {
        // Retrieve values of all fields
        Class cl = getClass();
        for (Field field : cl.getDeclaredFields()) {
            field.setAccessible(true);

            String name = field.getName();

            try {
                // Make sure that we have only Constants in this
                // ConstantSet
                Object obj = field.get(this);

                if (obj instanceof Constant) {
                    Constant constant = (Constant) obj;
                    constant.setUnit(unit);
                    constant.setName(name);
                    map.put(name, constant);
                } else {
                    logger.severe("ConstantSet in unit '" + unit
                                  + "' contains a non Constant field '"
                                  + name + "' obj= " + obj);
                }
            } catch (IllegalAccessException ex) {
                // Cannot occur in fact, thanks to setAccessible
            }
        }

        // Dump the current constant values for this unit, if so asked for
        if (logger.isDebugEnabled()
            || (new Constant.Boolean(unit, "constantValues", false,
                                     "Debugging flag for ConstantSet")
                .getValue())) {
            dump();
        }
    }

    //-------------//
    // getConstant //
    //-------------//
    /**
     * Report a constant knowing its name in the constant set
     *
     * @param name the desired name
     *
     * @return the proper constant, or null if not found
     */
    Constant getConstant (String name)
    {
        return map.get(name);
    }

    //-------------//
    // getConstant //
    //-------------//
    /**
     * Report a constant knowing its index in the constant set
     *
     * @param i the desired index value
     *
     * @return the proper constant
     */
    Constant getConstant (int i)
    {
        return Collections.list
            (Collections.enumeration(map.values())).get(i);
    }

    //------//
    // size //
    //------//
    /**
     * Report the number of constants in this constant set
     *
     * @return the size of the constant set
     */
    int size ()
    {
        return map.size();
    }

    //---------//
    // getName //
    //---------//

    /**
     * Report the name of the enclosing unit
     *
     * @return unit name
     */
    String getName ()
    {
        return unit;
    }
}
