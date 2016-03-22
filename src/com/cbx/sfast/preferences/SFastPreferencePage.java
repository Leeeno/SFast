package com.cbx.sfast.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import sfast.Activator;

import com.cbx.sfast.utilities.CbxUtil;
import com.cbx.sfast.utilities.MultiLineTextFieldEditor;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By subclassing
 * <samp>FieldEditorPreferencePage</samp>, we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that belongs to the main
 * plug-in class. That way, preferences can be accessed directly via the preference store.
 */

public class SFastPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public SFastPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Tools for cbx");
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to manipulate various
     * types of preferences. Each field editor knows how to save and restore itself.
     */
    @Override
    public void createFieldEditors() {
        // addField(new DirectoryFieldEditor(PreferenceConstants.P_PATH,
        // "&Directory preference:", getFieldEditorParent()));
        addField(new StringFieldEditor(PreferenceConstants.P_CMD, "命令行程序(CMD或路径)：", getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.P_REWRITE_JETTY_CMD, "重写 jetty.debug.cmd",
                getFieldEditorParent()));
        //
        addField(new MultiLineTextFieldEditor(PreferenceConstants.P_JETTY_SCRIPT, "Jetty 脚本模板:", getFieldEditorParent()));

        addField(new RadioGroupFieldEditor(PreferenceConstants.P_FOR_LABEL, "运行biz时始终编译：", 1, new String[][] {},
                getFieldEditorParent()));

        addField(new BooleanFieldEditor(PreferenceConstants.P_SMART_BUILD, "自动感知", getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.P_ALWAYS_ANT_GENERAL, CbxUtil.NAME_GENERAL_PROJECT,
                getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.P_ALWAYS_ANT_UI, CbxUtil.NAME_UI_PROJECT,
                getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.P_ALWAYS_ANT_CORE, CbxUtil.NAME_CORE_PROJECT,
                getFieldEditorParent()));

        addField(new RadioGroupFieldEditor(PreferenceConstants.P_FOR_LABEL, "Ant Core 之后将jar包复制到：", 1,
                new String[][] {}, getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.P_CORE_JAR_TO_UI, CbxUtil.NAME_UI_PROJECT,
                getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.P_CORE_JAR_TO_GENERAL, CbxUtil.NAME_GENERAL_PROJECT,
                getFieldEditorParent()));

        addField(new RadioGroupFieldEditor(PreferenceConstants.P_FOR_LABEL, "Ant UI 之后将jar包复制到：", 1, new String[][] {},
                getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.P_UI_JAR_TO_GENERAL, CbxUtil.NAME_GENERAL_PROJECT,
                getFieldEditorParent()));
        final StringFieldEditor sfeBackupExclude = new StringFieldEditor(PreferenceConstants.P_BACKUP_EXCLUDE,
                "备份时排除：", getFieldEditorParent());
        sfeBackupExclude.getLabelControl(getFieldEditorParent()).setToolTipText(
                "使用|分隔多个条件，<表示startsWith，>表示endsWith，?表示contains，:表示equals识别空格。例如：>.jar|?snapshot");
        addField(sfeBackupExclude);
    }

    @Override
    public void init(final IWorkbench workbench) {
    }

}
