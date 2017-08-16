/*
 * Tencent is pleased to support the open source community by making Angel available.
 *
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.tencent.angel.spark.model

import org.apache.spark.SparkException

import com.tencent.angel.spark.client.PSClient

/**
 * PSVectorPool delegate a memory space on PS servers,
 * which hold `capacity` number vectors with `numDimensions` dimension.
 * The dimension of PSVectors in one PSVectorPool is the same.
 *
 * A PSVectorPool is like a Angel Matrix.
 *
 * @param id PSVectorPool unique id
 * @param numDimensions Dimension of vectors
 * @param capacity Capacity of pool
 */
abstract class PSModelPool(
    private[spark] val id: Int,
    val numDimensions: Int,
    val capacity: Int) {

  private[spark] def allocate(): PSModelProxy

  /**
   * Create a PSVector with a local Double Array
   *
   * @param value The local Double Array
   */
  def createModel(value: Array[Double]): PSModelProxy = {
    assertCompatible(value)
    val vector = allocate()
    PSClient().push(vector, value)
    vector
  }

  /**
   * Create a PSVector filled with a local Double value
   *
   * @param value the local Double value
   */
  def createModel(value: Double): PSModelProxy = {
    val vector = allocate()
    PSClient().fill(vector, value)
    vector
  }

  /**
   * Return a zero PSVector
   */
  def createZero(): PSModelProxy = createModel(0.0)

  /**
   * Create a random PSVector, the elements is generated by uniform distribution
   *
   * @param min the uniform distribution parameter: minimum boundary
   * @param max the uniform distribution parameter: maximum boundary
   */
  def createRandomUniform(min: Double, max: Double): PSModelProxy = {
    val vector = allocate()
    PSClient().randomUniform(vector, min, max)
    vector
  }

  /**
   * Create a random PSVector, the elements is generated by normal distribution
   *
   * @param mean the uniform distribution parameter: mean
   * @param stddev the uniform distribution parameter: standard deviation
   */
  def createRandomNormal(mean: Double, stddev: Double): PSModelProxy = {
    val vector = allocate()
    PSClient().randomNormal(vector, mean, stddev)
    vector
  }

  /**
   * Make sure dimension compatible
   */
  private def assertCompatible(other: Array[Double]): Unit = {
    if (this.numDimensions != other.length) {
      throw new SparkException(s"The target array's dimension " +
        s"does not match this vector pool! \n" +
        s"pool dimension is $numDimensions," +
        s"but target array's dimension is ${other.length}")
    }
  }

  /**
   * Delete a PSVector from this PSVectorPool
   */
  private[spark] def delete(key: PSModelProxy): Unit

  /**
   * Destroy this PSVectorPool
   */
  private[spark] def destroy(): Unit

}