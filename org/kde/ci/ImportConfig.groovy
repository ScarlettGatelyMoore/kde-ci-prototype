/**
 * 
 */
package org.kde.ci

/**
# <DSL Configuration Yaml Parser for KDE Continous Integration System>
# Copyright 2016  Scarlett Clark <sgclark@kde.org>
# 
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License as
# published by the Free Software Foundation; either version 2 of
# the License or (at your option) version 3 or any later version
# accepted by the membership of KDE e.V. (or its successor approved
# by the membership of KDE e.V.), which shall act as a proxy
# defined in Section 14 of version 3 of the license.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
# 
#
*/
import groovy.io.FileType
/*@Grapes([
 @Grab('org.yaml:snakeyaml:1.13'),
 @Grab('com.google.guava:guava:18.0'),
 //@GrabConfig(systemClassLoader = true)
])*/

import org.yaml.snakeyaml.Yaml
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Provider.Service
import com.google.common.io.ByteStreams


class ImportConfig {
	
	ImportConfig() {}
	
	def ArrayList genListOfFilesinDir(filename) {
		def repofiles
		def dir = new File(filename)
		dir.eachFileRecurse (FileType.FILES) { file ->
		  repofiles << file
		}
		return repofiles
	}

	def Object getConfig(file) throws IOException {
		String configFile
		configFile = path + file
		assert configFile : "Invalid Config File" + configFile
		def yamldata = new Yaml().load(new FileReader(new File(configFile)))		
		return yamldata
	}
}
