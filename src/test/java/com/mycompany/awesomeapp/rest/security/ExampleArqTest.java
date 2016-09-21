package com.mycompany.awesomeapp.rest.security;


import org.hamcrest.core.Is;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * przykladowy test w arquillian
 */
@RunWith(Arquillian.class)
public class ExampleArqTest {

    /**
     * utworz paczke jar do testu
     *
     * @return jar
     */
    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
                .addClass(ExampleArqTest.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        /**
         * wrzuc do logow testu liste plikow z paczki
         */
        System.out.println(archive.toString(true));
        return archive;
    }

    /**
     * prosty test z uzyciem JUnit i Hamcrest
     */
    @Test
    public void exampleArquillianTest() {
        Assert.assertThat("check if true is true (Arquillian JUnit/Hamcrest Example Test)", /* is */ true, /* expected is */ Is.is(true));
    }
}
