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

package com.lightbend.modelServer.model.tensorflow

import com.lightbend.model.modeldescriptor.ModelDescriptor
import com.lightbend.model.winerecord.WineRecord
import com.lightbend.modelServer.ModelToServe
import com.lightbend.modelServer.model.{Model, ModelFactory}
import org.tensorflow.{Graph, Session, Tensor}

/**
  * Created by boris on 5/26/17.
  * Implementation of tensorflow model
  */

class TensorFlowModel(inputStream : Array[Byte]) extends Model{

  val graph = new Graph
  graph.importGraphDef(inputStream)
  val session = new Session(graph)

  override def score(input: AnyVal): AnyVal = {

    val record = input.asInstanceOf[WineRecord]
    val data = Array(
      record.fixedAcidity.toFloat,
      record.volatileAcidity.toFloat,
      record.citricAcid.toFloat,
      record.residualSugar.toFloat,
      record.chlorides.toFloat,
      record.freeSulfurDioxide.toFloat,
      record.totalSulfurDioxide.toFloat,
      record.density.toFloat,
      record.pH.toFloat,
      record.sulphates.toFloat,
      record.alcohol.toFloat
    )
    val modelInput = Tensor.create(Array(data))
    val result = session.runner.feed("dense_1_input", modelInput).fetch("dense_3/Sigmoid").run().get(0)
    val rshape = result.shape
    var rMatrix = Array.ofDim[Float](rshape(0).asInstanceOf[Int],rshape(1).asInstanceOf[Int])
    result.copyTo(rMatrix)
    var value = (0, rMatrix(0)(0))
    1 to (rshape(1).asInstanceOf[Int] -1) foreach{i => {
      if(rMatrix(0)(i) > value._2)
        value = (i, rMatrix(0)(i))
    }}
    value._1.toDouble
  }

  override def cleanup(): Unit = {
    try{
      session.close
    }catch {
      case t: Throwable =>    // Swallow
    }
    try{
      graph.close
    }catch {
      case t: Throwable =>    // Swallow
    }
  }

  override def toBytes(): Array[Byte] = graph.toGraphDef

  override def getType: Long = ModelDescriptor.ModelType.TENSORFLOW.value
}

object TensorFlowModel extends  ModelFactory {
  def apply(inputStream: Array[Byte]): Option[TensorFlowModel] = {
    try {
      Some(new TensorFlowModel(inputStream))
    }catch{
      case t: Throwable => None
    }
  }

  override def create(input: ModelToServe): Option[Model] = {
    try {
      Some(new TensorFlowModel(input.model))
    }catch{
      case t: Throwable => None
    }
  }

  override def restore(bytes: Array[Byte]): Model = new TensorFlowModel(bytes)
}
