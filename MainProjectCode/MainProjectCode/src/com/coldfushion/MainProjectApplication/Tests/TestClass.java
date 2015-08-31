package com.coldfushion.MainProjectApplication.Tests;

import android.content.Context;
import android.os.AsyncTask;
import android.test.InstrumentationTestCase;
import com.coldfushion.MainProjectApplication.Activities.ResultActivity;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import static com.coldfushion.MainProjectApplication.Activities.ResultActivity.*;
import static org.junit.Assert.*;

/**
 * Created by Kraaijeveld on 18-6-2015.
 */
public class TestClass
{

    @Test
    public void noInternetErrorsShouldBeUnCaught() throws Exception
    {
        ResultActivity ResultActivityToBeTested =  new ResultActivity();
        if(ResultActivityToBeTested.isOnline() == false)
        {
            //the toast must be made or else the test fails
            assertFalse(ResultActivityToBeTested.t.equals(null));
        }
    }

    @Test
    public void jsonExceptionsShouldBeCaught() throws Exception
    {

    }

    @Test
    public void testIsOnline() throws Exception
    {

    }

}