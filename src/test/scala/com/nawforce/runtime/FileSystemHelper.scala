/*
 [The "BSD licence"]
 Copyright (c) 2019 Kevin Jones
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.nawforce.runtime

import com.nawforce.common.path.{PathFactory, PathLike}
import com.nawforce.pkg.imports.{FSMonkey, Memfs}
import com.nawforce.runtime.types.PlatformTypeDeclaration

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

object FileSystemHelper {

  // Abstract virtual filesystem for testing
  def run[T](files: Map[String, String])(verify: PathLike => T): T = {
    // Force loaded of all platform types before messing with Fs
    PlatformTypeDeclaration.all

    val unpatch = FSMonkey.patchFs(Memfs.vol)
    try {
      Memfs.vol.fromJSON(files.map(kv => ("/" + kv._1, kv._2)).toJSDictionary.asInstanceOf[js.Dynamic])
      verify(PathFactory("/"))
    } finally {
      unpatch()
      Memfs.vol.reset()
    }
  }
}
