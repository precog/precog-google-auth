/*
 * Copyright 2020 Precog Data
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.precog.googleauth

import cats.effect.Sync
import cats.implicits._

import java.io.ByteArrayInputStream
import scala.{Array, Byte, Predef}, Predef._

import com.google.auth.oauth2.{AccessToken, GoogleCredentials}

object Credentials {
  def googleCredentials[F[_]: Sync](auth: Array[Byte], scopes: String*): F[GoogleCredentials] =
    Sync[F] delay {
      GoogleCredentials
        .fromStream(new ByteArrayInputStream(auth))
        .createScoped(scopes: _*)
    }

  def accessToken[F[_]: Sync](auth: Array[Byte], scopes: String*): F[AccessToken] = 
    googleCredentials[F](auth, scopes: _*).flatMap(creds =>
      Sync[F].delay(creds.refreshIfExpired()) >>
        Sync[F].delay(creds.refreshAccessToken()))
}
