/*
 * Copyright (c) 2017 Zac Sweers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sweers.catchup.util.data.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonAdapter.Factory
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Types
import org.unbescape.html.HtmlEscape
import org.unbescape.java.JavaEscape

/**
 * [JsonAdapter] that defaults the given element if it is a collection to an empty form
 * if it is null, denoted via [UnEscape].
 */
class UnescapeJsonAdapter constructor(
    private val delegate: JsonAdapter<String>,
    private val html: Boolean) : JsonAdapter<String>() {

  override fun fromJson(reader: JsonReader): String? {
    val fromJson: String? = delegate.fromJson(reader)
    return if (html) {
      HtmlEscape.unescapeHtml(fromJson)
    } else {
      JavaEscape.unescapeJava(fromJson)
    }
  }

  override fun toJson(writer: JsonWriter, value: String?) {
    delegate.toJson(writer, value)
  }

  override fun toString() = "$delegate.unescaping(html=$html)"

  companion object {
    @JvmField
    val FACTORY = Factory { type, annotations, moshi ->
      if (annotations.size > 1) {
        // save the iterator
        return@Factory null
      }
      val unescape = annotations.find { it is UnEscape } ?: return@Factory null

      return@Factory UnescapeJsonAdapter(moshi.adapter(type,
          Types.nextAnnotations(annotations,
              UnEscape::class.java)!!),
          (unescape as UnEscape).html)
    }
  }
}
