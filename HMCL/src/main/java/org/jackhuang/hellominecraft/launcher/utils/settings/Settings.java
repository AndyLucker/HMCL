/*
 * Copyright 2013 huangyuhui <huanghongxun2008@126.com>
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.
 */
package org.jackhuang.hellominecraft.launcher.utils.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import org.jackhuang.hellominecraft.C;
import org.jackhuang.hellominecraft.HMCLog;
import org.jackhuang.hellominecraft.launcher.Main;
import org.jackhuang.hellominecraft.utils.EnumAdapter;
import org.jackhuang.hellominecraft.utils.tinystream.CollectionUtils;
import org.jackhuang.hellominecraft.utils.FileUtils;
import org.jackhuang.hellominecraft.utils.IOUtils;
import org.jackhuang.hellominecraft.utils.MessageBox;
import org.jackhuang.hellominecraft.utils.Platform;
import org.jackhuang.hellominecraft.utils.StrUtils;
import org.jackhuang.hellominecraft.utils.UpdateChecker;
import org.jackhuang.hellominecraft.utils.VersionNumber;

/**
 *
 * @author hyh
 */
public final class Settings {

    public static final File settingsFile = new File(IOUtils.currentDir(), "hmcl.json");
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Platform.class, new EnumAdapter<>(Platform.values())).create();

    private static boolean isFirstLoad;
    private static final Config settings;
    public static final UpdateChecker UPDATE_CHECKER;

    public static Config getInstance() {
        return settings;
    }

    public static boolean isFirstLoad() {
        return isFirstLoad;
    }

    static {
        settings = initSettings();
        isFirstLoad = StrUtils.isBlank(settings.getUsername());
        if (!getVersions().containsKey("Default"))
            getVersions().put("Default", new Profile());

        UPDATE_CHECKER = new UpdateChecker(new VersionNumber(Main.firstVer, Main.secondVer, Main.thirdVer),
                "hmcl", settings.isCheckUpdate(), () -> Main.invokeUpdate());
    }

    private static Config initSettings() {
        Config c = new Config();
        if (settingsFile.exists())
            try {
                String str = FileUtils.readFileToString(settingsFile);
                if (str == null || str.trim().equals(""))
                    HMCLog.log("Settings file is empty, use the default settings.");
                else {
                    Config d = gson.fromJson(str, Config.class);
                    if (d != null) c = d;
                }
                HMCLog.log("Initialized settings.");
            } catch (IOException | JsonSyntaxException e) {
                HMCLog.warn("Something happened wrongly when load settings.", e);
                if (MessageBox.Show(C.i18n("settings.failed_load"), MessageBox.YES_NO_OPTION) == MessageBox.NO_OPTION) {
                    HMCLog.err("Cancelled loading settings.");
                    System.exit(1);
                }
            }
        else {
            HMCLog.log("No settings file here, may be first loading.");
            isFirstLoad = true;
        }
        return c;
    }

    public static void save() {
        try {
            FileUtils.write(settingsFile, gson.toJson(settings));
        } catch (IOException ex) {
            HMCLog.err("Failed to save config", ex);
        }
    }

    public static Profile getVersion(String name) {
        return getVersions().get(name);
    }

    public static Map<String, Profile> getVersions() {
        return settings.getConfigurations();
    }

    public static void setVersion(Profile ver) {
        Objects.requireNonNull(ver);
        getVersions().put(ver.getName(), ver);
    }

    public static Collection<Profile> getProfiles() {
        return CollectionUtils.sortOut(getVersions().values(), (t) -> t != null && t.getName() != null);
    }

    public static Profile getOneProfile() {
        return settings.getConfigurations().firstEntry().getValue();
    }

    public static boolean trySetVersion(Profile ver) {
        if (ver == null || ver.getName() == null || getVersions().containsKey(ver.getName()))
            return false;
        getVersions().put(ver.getName(), ver);
        return true;
    }

    public static void delVersion(Profile ver) {
        delVersion(ver.getName());
    }

    public static void delVersion(String ver) {
        getVersions().remove(ver);
    }
}
