/*
 * Copyright 2014-2018 Groupon, Inc
 * Copyright 2014-2018 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.invoice.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.killbill.billing.invoice.InvoiceTestSuiteWithEmbeddedDB;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestInvoiceTrackingSqlDao extends InvoiceTestSuiteWithEmbeddedDB {

    @Test(groups = "slow")
    public void testBasicTrackingIds()  {
        final InvoiceTrackingSqlDao dao = dbi.onDemand(InvoiceTrackingSqlDao.class);

        LocalDate startRange = new LocalDate(2018, 8, 1);
        LocalDate endRange = new LocalDate(2018, 11, 23);

        final UUID invoiceId1 = UUID.randomUUID();
        final UUID invoiceId2 = UUID.randomUUID();
        final UUID subscriptionId = UUID.randomUUID();

        // Before desired range
        final InvoiceTrackingModelDao input0 = new InvoiceTrackingModelDao(UUID.randomUUID(), clock.getUTCNow(), "trackingId0", invoiceId1, subscriptionId, startRange.minusDays(1));

        final InvoiceTrackingModelDao input1 = new InvoiceTrackingModelDao(UUID.randomUUID(), clock.getUTCNow(), "trackingId1", invoiceId1, subscriptionId, startRange);
        final InvoiceTrackingModelDao input2 = new InvoiceTrackingModelDao(UUID.randomUUID(), clock.getUTCNow(), "trackingId2", invoiceId1, subscriptionId, new LocalDate(2018, 8, 5));
        final InvoiceTrackingModelDao input3 = new InvoiceTrackingModelDao(UUID.randomUUID(), clock.getUTCNow(), "trackingId3", invoiceId2, subscriptionId, new LocalDate(2018, 9, 1));

        // After desired range
        final InvoiceTrackingModelDao input4 = new InvoiceTrackingModelDao(UUID.randomUUID(), clock.getUTCNow(), "trackingId4", invoiceId1, subscriptionId, endRange);

        final List<InvoiceTrackingModelDao> inputs = new ArrayList<>();
        inputs.add(input0);
        inputs.add(input1);
        inputs.add(input2);
        inputs.add(input3);
        inputs.add(input4);

        dao.create(inputs, internalCallContext);

        final List<InvoiceTrackingModelDao> result = dao.getTrackingsByDateRange(startRange.toDate(), endRange.toDate(), internalCallContext);
        Assert.assertEquals(result.size(), 3);

        Assert.assertEquals(result.get(0).getTrackingId(), "trackingId1");
        Assert.assertEquals(result.get(0).getInvoiceId(), invoiceId1);
        Assert.assertEquals(result.get(0).getRecordDate(), startRange);
        Assert.assertEquals(result.get(0).getSubscriptionId(), subscriptionId);

        Assert.assertEquals(result.get(1).getTrackingId(), "trackingId2");
        Assert.assertEquals(result.get(1).getInvoiceId(), invoiceId1);
        Assert.assertEquals(result.get(1).getRecordDate(), new LocalDate(2018, 8, 5));
        Assert.assertEquals(result.get(1).getSubscriptionId(), subscriptionId);

        Assert.assertEquals(result.get(2).getTrackingId(), "trackingId3");
        Assert.assertEquals(result.get(2).getInvoiceId(), invoiceId2);
        Assert.assertEquals(result.get(2).getRecordDate(), new LocalDate(2018, 9, 1));
        Assert.assertEquals(result.get(2).getSubscriptionId(), subscriptionId);

    }
}
