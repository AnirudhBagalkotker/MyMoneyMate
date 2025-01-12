package com.mymoneymate.utils;

import java.awt.Font;

import javax.swing.UIManager;

public class FontUtils {
	public static void setUIFont(Font font) {
		UIManager.put("Label.font", font);
		UIManager.put("Button.font", font);
		UIManager.put("TextField.font", font);
		UIManager.put("TextArea.font", font);
		UIManager.put("Table.font", font);
		UIManager.put("List.font", font);
		UIManager.put("ComboBox.font", font);
		UIManager.put("Menu.font", font);
		UIManager.put("MenuItem.font", font);
		UIManager.put("CheckBox.font", font);
		UIManager.put("RadioButton.font", font);
	}
}
