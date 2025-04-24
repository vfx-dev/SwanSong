/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.config;

import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.config.ConfigurationManager;
import com.ventooth.swansong.Share;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import cpw.mods.fml.client.config.DummyConfigElement;
import cpw.mods.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Configs {
    public static void poke() {
    }

    static {
        init();
    }

    @SneakyThrows
    private static void init() {
        ConfigurationManager.initialize(cfgClasses());
    }

    public static void syncConfigFile() {
        Share.log.debug("Syncing Configs");

        val types = cfgClasses();
        try {
            ConfigurationManager.saveToFile(true, types);
        } catch (ConfigException e) {
            Share.log.error("Failed to save config to file:", e);
        }
        try {
            ConfigurationManager.loadFromFile(types);
        } catch (ConfigException e) {
            Share.log.error("Failed to reload config from file:", e);
        }

        Share.log.debug("Synced Configs");
    }

    static Class<?>[] cfgClasses() {
        val categories = cfgCategories(true);
        val types = new ArrayList<Class<?>>();
        for (val category : categories) {
            types.add(category.type());
        }
        return types.toArray(new Class<?>[0]);
    }

    @SuppressWarnings("rawtypes")
    static List<IConfigElement> cfgElements() throws ConfigException {
        val categories = Configs.cfgCategories(false);
        val elements = new ArrayList<IConfigElement>();
        for (val category : categories) {
            elements.add(category.asElement());
        }
        return elements;
    }

    static List<CfgCategory> cfgCategories(boolean init) {
        val categories = new ArrayList<CfgCategory>();
        categories.add(new CfgCategory("00_modules", "config.swansong.modules.Modules", ModuleConfig.class));

        if (init || ModuleConfig.Debug) {
            categories.add(new CfgCategory("01_debug", "config.swansong.modules.Debug", DebugConfig.class));
        }
        categories.add(new CfgCategory("02_shaders", "config.swansong.modules.Shaders", ShadersConfig.class));
        categories.add(new CfgCategory("03_compat", "config.swansong.modules.Compat", CompatConfig.class));

        return categories;
    }

    //TODO convert to record
    static final class CfgCategory {
        private final String name;
        private final String lang;
        private final Class<?> type;

        CfgCategory(String name, String lang, Class<?> type) {
            this.name = name;
            this.lang = lang;
            this.type = type;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        IConfigElement asElement() throws ConfigException {
            val innerElements = ConfigurationManager.getConfigElementsMulti(type);
            return new DummyConfigElement.DummyCategoryElement(name, lang, innerElements);
        }

        public String name() {
            return name;
        }

        public String lang() {
            return lang;
        }

        public Class<?> type() {
            return type;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (CfgCategory) obj;
            return Objects.equals(this.name, that.name) &&
                   Objects.equals(this.lang, that.lang) &&
                   Objects.equals(this.type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, lang, type);
        }

        @Override
        public String toString() {
            return "CfgCategory[" + "name=" + name + ", " + "lang=" + lang + ", " + "type=" + type + ']';
        }

    }
}
