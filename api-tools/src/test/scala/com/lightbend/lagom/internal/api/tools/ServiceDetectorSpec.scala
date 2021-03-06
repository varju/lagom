/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.internal.api.tools

import com.lightbend.lagom.javadsl.api.{ Descriptor, Service }
import org.scalatest._
import play.api.libs.json.Json

class ServiceDetectorSpec extends WordSpec with Matchers with Inside {

  "The service detector" should {

    "resolve the service descriptions for a lagom project" in {
      val expectedJsonString =
        """
          |[
          |  {
          |    "name": "/aclservice",
          |    "acls": [
          |      {
          |        "method": "GET",
          |        "pathPattern": "\\Q/mocks/\\E([^/]+)"
          |      },
          |      {
          |        "method": "POST",
          |        "pathPattern": "\\Q/mocks\\E"
          |      }
          |    ]
          |  },
          |  {
          |    "name": "/noaclservice",
          |    "acls": []
          |  }
          |]
        """.stripMargin

      val classLoader = Thread.currentThread().getContextClassLoader()
      val actualJsonString = ServiceDetector.services(classLoader)
      Json.parse(actualJsonString) shouldBe Json.parse(expectedJsonString)
    }

    "resolve the service interface based on a service implementation" in {
      trait ServiceInterface extends Service {
        override def descriptor(): Descriptor = null
      }
      class ServiceImpl extends ServiceInterface

      ServiceDetector.serviceInterfaceResolver(classOf[ServiceImpl]) shouldBe Some(classOf[ServiceInterface])
    }

    "resolve the parent service interface that has implemented the descriptor method" in {
      trait ParentServiceInterface extends Service {
        override def descriptor(): Descriptor = null
      }
      trait ChildServiceInterface extends ParentServiceInterface
      class ServiceImpl extends ChildServiceInterface

      ServiceDetector.serviceInterfaceResolver(classOf[ServiceImpl]) shouldBe Some(classOf[ParentServiceInterface])
    }

    "resolve the child service interface that has implemented the descriptor method" in {
      trait ParentServiceInterface extends Service
      trait ChildServiceInterface extends ParentServiceInterface {
        override def descriptor(): Descriptor = null
      }
      class ServiceImpl extends ChildServiceInterface

      ServiceDetector.serviceInterfaceResolver(classOf[ServiceImpl]) shouldBe Some(classOf[ChildServiceInterface])
    }

    def minify(s: String) =
      s.replace(" ", "").replace("\n", "")
  }
}
