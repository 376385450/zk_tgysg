package com.sinohealth.common.utils.ip;

import org.junit.Assert;
import org.junit.Test;

import java.net.InetAddress;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2023-12-01 10:09
 */
public class IpUtilsTest {

    @Test
    public void testGetIpAddr() throws Exception {
        String result = IpUtils.getIpAddr(null);
        Assert.assertEquals("replaceMeWithExpectedResult", result);
    }

    @Test
    public void testInternalIp() throws Exception {
        boolean result = IpUtils.internalIp("ip");
        Assert.assertEquals(true, result);
    }

    @Test
    public void testTextToNumericFormatV4() throws Exception {
        byte[] result = IpUtils.textToNumericFormatV4("text");
        Assert.assertArrayEquals(new byte[]{(byte) 0}, result);
    }

    @Test
    public void testGetHostIp() throws Exception {
        InetAddress localHost = InetAddress.getLocalHost();

        String result = IpUtils.getHostIp();
        Assert.assertEquals("replaceMeWithExpectedResult", result);
    }

    @Test
    public void testGetHostName() throws Exception {
        String result = IpUtils.getHostName();
        Assert.assertEquals("replaceMeWithExpectedResult", result);
    }
}
