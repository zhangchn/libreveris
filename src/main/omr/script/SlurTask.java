//----------------------------------------------------------------------------//
//                                                                            //
//                              S l u r T a s k                               //
//                                                                            //
//  Copyright (C) Herve Bitteur 2000-2007. All rights reserved.               //
//  This software is released under the GNU General Public License.           //
//  Contact author at herve.bitteur@laposte.net to report bugs & suggestions. //
//----------------------------------------------------------------------------//
//
package omr.script;

import omr.glyph.Glyph;
import static omr.script.ScriptRecording.*;

import omr.sheet.Sheet;

import omr.step.StepException;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Class <code>SlurTask</code> is a script task which attempts to fix a slur
 * glyph (by extracting a new glyph out of some sections of the old glyph)
 *
 * @author Herv&eacute Bitteur
 * @version $Id$
 */
@XmlAccessorType(XmlAccessType.NONE)
public class SlurTask
    extends GlyphTask
{
    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SlurTask object.
     *
     * @param glyphs the collection of glyphs to process
     */
    public SlurTask (Collection<Glyph> glyphs)
    {
        super(glyphs);
    }

    //----------//
    // SlurTask //
    //----------//
    /** No-arg constructor needed by JAXB */
    private SlurTask ()
    {
    }

    //~ Methods ----------------------------------------------------------------

    //-----//
    // run //
    //-----//
    @Override
    public void run (Sheet sheet)
        throws StepException
    {
        super.run(sheet);
        sheet.getSymbolsBuilder()
             .fixLargeSlurs(glyphs, RECORDING);
    }

    //-----------------//
    // internalsString //
    //-----------------//
    @Override
    protected String internalsString ()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" slur");

        return sb.toString() + super.internalsString();
    }
}
