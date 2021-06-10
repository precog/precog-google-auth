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

import java.lang.RuntimeException
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Paths}
import scala.{Array, Byte, Left, Right}
import scala.Predef.String

import argonaut._, Argonaut._
import cats.effect.Sync
import cats.implicits._

final case class Url(value: String)

final case class ServiceAccount(
    tokenUri: Url,
    authProviderCertUrl: Url,
    privateKey: String,
    clientId: String,
    clientCertUrl: Url,
    authUri: Url,
    projectId: String,
    privateKeyId: String,
    clientEmail: String,
    accountType: String) {

  val serviceAccountAuthBytes: Array[Byte] = this.asJson.toString.getBytes("UTF-8")
}

object ServiceAccount {

  implicit val serviceAccountCodecJson: CodecJson[ServiceAccount] =
    casecodec10[String, String, String, String, String, String, String, String, String, String, ServiceAccount](
      (tokenUri,
      authProviderCertUrl,
      privateKey,
      clientId,
      clientCertUrl,
      authUri,
      projectId,
      privateKeyId,
      clientEmail,
      accountType) => ServiceAccount(
        tokenUri = Url(tokenUri),
        authProviderCertUrl = Url(authProviderCertUrl),
        privateKey = privateKey,
        clientId = clientId,
        clientCertUrl = Url(clientCertUrl),
        authUri = Url(authUri),
        projectId = projectId,
        privateKeyId = privateKeyId,
        clientEmail = clientEmail,
        accountType = accountType),
      sac =>
        (sac.tokenUri.value,
        sac.authProviderCertUrl.value,
        sac.privateKey,
        sac.clientId,
        sac.clientCertUrl.value,
        sac.authUri.value,
        sac.projectId,
        sac.privateKeyId,
        sac.clientEmail,
        sac.accountType).some)(
          "token_uri",
          "auth_provider_x509_cert_url",
          "private_key",
          "client_id",
          "client_x509_cert_url",
          "auth_uri",
          "project_id",
          "private_key_id",
          "client_email",
          "type")

  val Redacted = "<REDACTED>"
  val RedactedUri = Url("REDACTED")
  val EmptyUri = Url("")

  val SanitizedAuth: ServiceAccount = ServiceAccount(
    tokenUri = RedactedUri,
    authProviderCertUrl = RedactedUri,
    clientCertUrl = RedactedUri,
    authUri = RedactedUri,
    privateKey = Redacted,
    clientId = Redacted,
    projectId = Redacted,
    privateKeyId = Redacted,
    clientEmail = Redacted,
    accountType = Redacted)

  val EmptyAuth: ServiceAccount = ServiceAccount(
    tokenUri = EmptyUri,
    authProviderCertUrl = EmptyUri,
    clientCertUrl = EmptyUri,
    authUri = EmptyUri,
    privateKey = "",
    clientId = "",
    projectId = "",
    privateKeyId = "",
    clientEmail = "",
    accountType = "")

  def fromResourceName[F[_]: Sync](authResourceName: String): F[ServiceAccount] =
    for {
      authCfgPath <- Sync[F].delay(Paths.get(getClass.getClassLoader.getResource(authResourceName).toURI))
      authCfgString <- Sync[F].delay(new String(Files.readAllBytes(authCfgPath), UTF_8))
      sa <-
        Parse.parse(authCfgString) match {
          case Left(_) => Sync[F].raiseError[ServiceAccount](new RuntimeException("Malformed auth config"))
          case Right(json) => json.as[ServiceAccount].fold(
            (_, _) => Sync[F].raiseError[ServiceAccount](new RuntimeException("Json is not valid ServiceAccount")),
            _.pure[F])
        }
    } yield sa

}