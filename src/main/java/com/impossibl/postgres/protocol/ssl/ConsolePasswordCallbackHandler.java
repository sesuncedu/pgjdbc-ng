/**
 * Copyright (c) 2013, impossibl.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of impossibl.com nor the names of its contributors may
 *    be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.impossibl.postgres.protocol.ssl;

import com.impossibl.postgres.system.Context;

import static com.impossibl.postgres.system.Settings.SSL_PASSWORD;

import java.io.Console;
import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;



public class ConsolePasswordCallbackHandler implements ContextCallbackHandler {

  private char[] password;

  @Override
  public void init(Context conn) {

    String password = conn.getSetting(SSL_PASSWORD, String.class);
    if (password != null) {
      this.password = password.toCharArray();
    }

  }

  @Override
  public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

    Console cons = System.console();
    if (cons == null && password == null) {
      throw new UnsupportedCallbackException(callbacks[0], "Console is not available");
    }

    for (int i = 0; i < callbacks.length; i++) {

      if (callbacks[i] instanceof PasswordCallback) {

        PasswordCallback passwordCallback = (PasswordCallback) callbacks[i];

        if (password == null) {
          // It is used instead of cons.readPassword(prompt), because the prompt
          // may contain '%' characters
          passwordCallback.setPassword(cons.readPassword("%s", new Object[] {((PasswordCallback) callbacks[i]).getPrompt()}));
        }
        else {
          passwordCallback.setPassword(password);
        }
      }
      else {
        throw new UnsupportedCallbackException(callbacks[i]);
      }

    }

  }

}
