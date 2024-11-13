/**
 * NetXMS - open source network management system
 * Copyright (C) 2003-2023 Raden Solutions
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
package org.netxms.nxmc.base.windows;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.netxms.nxmc.PreferenceStore;
import org.netxms.nxmc.Startup;
import org.netxms.nxmc.base.views.View;
import org.netxms.nxmc.base.views.ViewStack;
import org.netxms.nxmc.base.widgets.MessageArea;
import org.netxms.nxmc.base.widgets.MessageAreaHolder;
import org.netxms.nxmc.keyboard.KeyBindingManager;
import org.netxms.nxmc.keyboard.KeyStroke;
import org.netxms.nxmc.localization.LocalizationHelper;
import org.xnap.commons.i18n.I18n;

/**
 * Window that holds pop out view
 */
public class PopOutViewWindow extends Window implements MessageAreaHolder
{
   /**
    * Open pop-out window with given view
    *
    * @param view view to show in pop-out window
    */
   public static void open(View view)
   {
      open(view, false, false);
   }

   /**
    * Open pop-out window with given view
    *
    * @param view view to show in pop-out window
    * @param fullScreen true to show full screen window
    * @param blocking block on open
    */
   public static void open(View view, boolean fullScreen, boolean blocking)
   {
      PopOutViewWindow w = new PopOutViewWindow(view, fullScreen);
      w.setBlockOnOpen(blocking);
      w.open();
   }

   private final I18n i18n = LocalizationHelper.getI18n(PopOutViewWindow.class);

   private Composite windowArea;
   private MessageArea messageArea;
   private ViewStack viewContainer;
   private View view;
   private Action actionToggleFullScreen;
   private KeyBindingManager keyBindingManager;
   private boolean openInFullScreen;

   /**
    * Create new pop-out window.
    *
    * @param view view to show inside pop-out window
    */
   private PopOutViewWindow(View view, boolean openInFullScreen)
   {
      super((Shell)null);
      this.view = view;
      this.openInFullScreen = openInFullScreen;
      this.keyBindingManager = new KeyBindingManager();
   }

   /**
    * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
    */
   @Override
   protected void configureShell(Shell shell)
   {
      super.configureShell(shell);
      shell.setText(view.getFullName());
      Point shellSize = PreferenceStore.getInstance().getAsPoint("PopupWindowSize." + view.getBaseId(), null);
      if (shellSize != null)
         shell.setSize(shellSize);
      shell.setImages(Startup.windowIcons);
      shell.setFullScreen(openInFullScreen);
   }

   /**
    * @see org.eclipse.jface.window.Window#getLayout()
    */
   @Override
   protected Layout getLayout()
   {
      return new FillLayout();
   }

   /**
    * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
    */
   @Override
   protected Control createContents(Composite parent)
   {
      actionToggleFullScreen = new Action(i18n.tr("&Full screen"), Action.AS_CHECK_BOX) {
         @Override
         public void run()
         {
            getShell().setFullScreen(actionToggleFullScreen.isChecked());
         }
      };
      actionToggleFullScreen.setChecked(openInFullScreen);
      keyBindingManager.addBinding("F11", actionToggleFullScreen);

      windowArea = new Composite(parent, SWT.NONE);
      GridLayout layout = new GridLayout();
      layout.marginHeight = 0;
      layout.marginWidth = 0;
      layout.verticalSpacing = 0;
      windowArea.setLayout(layout);

      messageArea = new MessageArea(windowArea, SWT.NONE);
      messageArea.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

      viewContainer = new ViewStack(this, null, windowArea, false, false, false) {
         @Override
         public Point computeSize(int wHint, int hHint, boolean changed)
         {
            Point size = super.computeSize(wHint, hHint, changed);
            if ((wHint == SWT.DEFAULT) && (size.x < 100))
               size.x = 800;
            if ((hHint == SWT.DEFAULT) && (size.y < 100))
               size.y = 600;
            return size;
         }
      };
      viewContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      viewContainer.setContextAware(false); // Preserve view's context if any
      viewContainer.setView(view);
      parent.addDisposeListener(new DisposeListener() {
         @Override
         public void widgetDisposed(DisposeEvent e)
         {
            PreferenceStore.getInstance().set("PopupWindowSize." + view.getBaseId(), getShell().getSize());
         }
      });

      final Display display = parent.getDisplay();
      Listener keyFilter = new Listener() {
         @Override
         public void handleEvent(Event e)
         {
            if (getShell() == display.getActiveShell()) // Only process keystrokes directed to this window
               processKeyDownEvent(e.stateMask, e.keyCode);
         }
      };
      display.addFilter(SWT.KeyDown, keyFilter);
      getShell().addDisposeListener(new DisposeListener() {
         @Override
         public void widgetDisposed(DisposeEvent e)
         {
            display.removeFilter(SWT.KeyDown, keyFilter);
            viewContainer.setEnabled(false); // To prevent calling setFocus() on views during disposal
         }
      });

      return parent;
   }

   /**
    * Process key down event
    *
    * @param e event to process
    */
   private void processKeyDownEvent(int stateMask, int keyCode)
   {
      if ((keyCode == SWT.SHIFT) || (keyCode == SWT.CTRL) || (keyCode == SWT.SHIFT) || (keyCode == SWT.ALT) || (keyCode == SWT.ALT_GR) || (keyCode == SWT.COMMAND))
         return; // Ignore key down on modifier keys

      KeyStroke ks = new KeyStroke(stateMask, keyCode);
      if (!keyBindingManager.processKeyStroke(ks))
         viewContainer.processKeyStroke(ks);
   }

   /**
    * @see org.netxms.nxmc.base.widgets.MessageAreaHolder#addMessage(int, java.lang.String)
    */
   @Override
   public int addMessage(int level, String text)
   {
      return messageArea.addMessage(level, text);
   }

   /**
    * @see org.netxms.nxmc.base.widgets.MessageAreaHolder#addMessage(int, java.lang.String, boolean)
    */
   @Override
   public int addMessage(int level, String text, boolean sticky)
   {
      return messageArea.addMessage(level, text, sticky);
   }

   /**
    * @see org.netxms.nxmc.base.widgets.MessageAreaHolder#addMessage(int, java.lang.String, boolean, java.lang.String, java.lang.Runnable)
    */
   @Override
   public int addMessage(int level, String text, boolean sticky, String buttonText, Runnable action)
   {
      return messageArea.addMessage(level, text, sticky, buttonText, action);
   }

   /**
    * @see org.netxms.nxmc.base.widgets.MessageAreaHolder#deleteMessage(int)
    */
   @Override
   public void deleteMessage(int id)
   {
      messageArea.deleteMessage(id);
   }

   /**
    * @see org.netxms.nxmc.base.widgets.MessageAreaHolder#clearMessages()
    */
   @Override
   public void clearMessages()
   {
      messageArea.clearMessages();
   }
}
