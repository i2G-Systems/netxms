/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2021 Victor Kirhenshtein
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.netxms.nxmc.modules.nxsl.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.netxms.nxmc.localization.LocalizationHelper;
import org.netxms.nxmc.modules.nxsl.widgets.ScriptEditor;
import org.xnap.commons.i18n.I18n;

/**
 * Dialog for edit script
 */
public class ScriptEditDialog extends Dialog
{
   private final I18n i18n = LocalizationHelper.getI18n(ScriptEditDialog.class);

   private String script;
   private ScriptEditor editor;
   private String hints;
   
   /**
    * @param parentShell
    * @param script
    */
   public ScriptEditDialog(Shell parentShell, String script, String hints)
   {
      super(parentShell);
      this.script = script;
      this.hints = hints;
   }

   /**
    * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
    */
   @Override
   protected void configureShell(Shell newShell)
   {
      super.configureShell(newShell);
      newShell.setText(i18n.tr("Edit Script"));
   }

   /**
    * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
    */
   @Override
   protected Control createDialogArea(Composite parent)
   {
      Composite area = (Composite)super.createDialogArea(parent);
      
      editor = new ScriptEditor(area, SWT.BORDER, SWT.H_SCROLL | SWT.V_SCROLL, hints);
      GridData gd = new GridData();
      gd.horizontalAlignment = SWT.FILL;
      gd.verticalAlignment = SWT.FILL;
      gd.grabExcessHorizontalSpace = true;
      gd.grabExcessVerticalSpace = true;
      gd.widthHint = 600;
      gd.heightHint = 400;
      editor.setLayoutData(gd);
      editor.setText(script);
      
      return area;
   }

   /**
    * @see org.eclipse.jface.dialogs.Dialog#okPressed()
    */
   @Override
   protected void okPressed()
   {
      script = editor.getText();
      super.okPressed();
   }

   /**
    * @return the script
    */
   public String getScript()
   {
      return script;
   }
}
