/**
 * 
 */
package org.kde.ci

import groovy.lang.Closure;

/**
# <Collection of DSL Closures to be used for KDE Continous Integration System>
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


class Platform {
	
	Platform() {		
	}
	
	def genBuildTrack(options, track) {
		def tracks = options.find { key, value -> key == 'tracks' }
			 if( tracks.getValue().toString().contains(track) ) {  return true }
			 else { return false }			
	}
	def genCompilers(options) {
		def compiler = options.find { key, value -> key == 'compilers' }.getValue()	
		def num = compiler.size()		
		if ( num > 1 ) {
			List compilers = compiler	
			return compilers 
		} else { 
			String acompiler = compiler.toString()
			return acompiler
		}
	}
	static String determineJobType(variations, compiler) {
		def jobType
		if (variations != null || compiler.getClass() == List) {
			jobType = 'matrixJob'
		} else {
			jobType = 'freeStyleJob'
		}
		return jobType
	}
	def PlatformVariations(options) {
		def var = options.find { key, value -> key == 'Variations' }.getValue()	
		if (var) {
			return var
		}
	}

}
