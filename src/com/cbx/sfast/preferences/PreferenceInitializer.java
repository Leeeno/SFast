package com.cbx.sfast.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import sfast.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_REWRITE_JETTY_CMD, true);
		store.setDefault(
				PreferenceConstants.P_JETTY_SCRIPT,
				"@echo off\r\nsetlocal\r\n"
						+ "set ANT_OPTS=-Xdebug -Xmx1024m -XX:MaxPermSize=3072m -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=4000,server=y,suspend=n"
						+ "\r\nant jetty.run 2> error.log\r\nendlocal");
	}

}
