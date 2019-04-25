/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2019, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 2110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.additional.testsuite.jdkall.present.ejb.lookup;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.eap.additional.testsuite.annotations.EapAdditionalTestsuite;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;

@RunWith(Arquillian.class)
@EapAdditionalTestsuite({"modules/testcases/jdkAll/Wildfly/ejb/src/main/java#17.0.0","modules/testcases/jdkAll/Eap72x/ejb/src/main/java#7.2.1","modules/testcases/jdkAll/Eap72x-Proposed/ejb/src/main/java","modules/testcases/jdkAll/Eap71x-Proposed/ejb/src/main/java","modules/testcases/jdkAll/Eap71x/ejb/src/main/java"})
public class OrbSlashLookupTestCase {

    @Test
    public void testStringToObject() throws Exception {
        // no point in creating our own object
        String iorAsString = "IOR:000000000000001d49444c3a43535f505054536572766963654d616e616765723a312e30000000000000000100000000000000720001000000000017663338747871312e67666f756e64726965732e636f6d0000c54400000000004a4a4d424900000013000000006469737061746368657200000000000000000024000000264152535500100016000c000465426f6100746865505054536572766963654d616e6167657200";
        ORB orb = ORB.init(new String[0], null);
        // this should not throw an IndexOutOfBounds exception
        Object corbaObj = orb.string_to_object(iorAsString);
        System.out.println("");
        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        System.out.println(corbaObj.toString());
        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        System.out.println("");
    }
}
