/*
  Copyright (c) 2013, impossibl.com
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:

   * Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.
   * Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in the
     documentation and/or other materials provided with the distribution.
   * Neither the name of impossibl.com nor the names of its contributors may
     be used to endorse or promote products derived from this software
     without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  POSSIBILITY OF SUCH DAMAGE.
 */
package com.impossibl.postgres.types;

import com.impossibl.postgres.protocol.ResultField.Format;

import java.lang.reflect.Array;
import java.util.Map;

/**
 * A database array type.
 *
 * @author kdubb
 *
 */
public class ArrayType extends Type {

  private Type elementType;

  public Type getElementType() {
    return elementType;
  }

  public void setElementType(Type elementType) {
    this.elementType = elementType;
  }

  @Override
  public PrimitiveType getPrimitiveType() {
    return PrimitiveType.Array;
  }

  @Override
  public Class<?> getJavaType(Format format, Map<String, Class<?>> customizations) {

    return Array.newInstance(elementType.getJavaType(format, customizations), 0).getClass();

  }

  @Override
  public boolean isParameterFormatSupported(Format format) {
    return elementType.isParameterFormatSupported(format);
  }

  @Override
  public boolean isResultFormatSupported(Format format) {
    return elementType.isResultFormatSupported(format);
  }

  @Override
  public Type unwrap() {
    return elementType;
  }

  public Type unwrapAll() {
    if (elementType instanceof ArrayType)
      return ((ArrayType) elementType).unwrapAll();
    return elementType;
  }

}
