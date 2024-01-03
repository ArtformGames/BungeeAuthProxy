/*
 * Copyright (C) 2022  Haowei Wen <yushijinhun@gmail.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.artformgames.injector.bungeeauthproxy;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Pattern;

public final class Logging {
    private Logging() {
    }

    private static final Pattern CONTROL_CHARACTERS_FILTER = Pattern.compile("\\p{Cc}&&[^\r\n\t]");

    public enum Level {
        DEBUG, INFO, WARNING, ERROR
    }

    public static void log(Level level, String message) {
        log(level, message, null);
    }

    public static void debug(String message) {
        log(Level.DEBUG, message);
    }

    public static void error(String message) {
        log(Level.ERROR, message);
    }

    public static void error(String message, Throwable e) {
        log(Level.ERROR, message, e);
    }

    public static void warning(String message) {
        log(Level.WARNING, message);
    }

    public static void log(Level level, String message, Throwable e) {
        if (level == Level.DEBUG && !Config.DEBUG.getNotNull()) return;

        String log = "[BungeeAuthProxy] [" + level + "] " + message;
        if (e != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();
            e.printStackTrace(pw);
            pw.close();
            log += sw.toString();
        }

        log = CONTROL_CHARACTERS_FILTER.matcher(log).replaceAll("");
        if (level == Level.ERROR) {
            System.err.println(log);
        } else {
            System.out.println(log);
        }
    }

}
