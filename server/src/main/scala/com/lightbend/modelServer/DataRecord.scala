/*
 * Copyright (C) 2017  Lightbend
 *
 * This file is part of flink-ModelServing
 *
 * flink-ModelServing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.lightbend.modelServer

import com.lightbend.model.winerecord.WineRecord

import scala.util.Try

/**
  * Created by boris on 5/8/17.
  */
object DataRecord {

  def fromByteArray(message: Array[Byte]): Try[WineRecord] = Try {
    WineRecord.parseFrom(message)
  }
}