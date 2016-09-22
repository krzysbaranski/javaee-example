package com.mycompany.awesomeapp.rest;

import com.mycompany.awesomeapp.model.Author;
import com.mycompany.awesomeapp.model.Book;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

@RunWith(Arquillian.class)
public class BookEndpointTest {

	@Inject
	private BookEndpoint bookEndpoint;

	@Deployment
	public static WebArchive createDeployment() {
		WebArchive webArchive = ShrinkWrap.create(WebArchive.class).addClass(BookEndpoint.class)
				.addClass(Book.class)
				.addClass(Author.class)
				//.addAsManifestResource("META-INF/persistence.xml", "persistence.xml")
				.addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
				.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
		System.out.println(webArchive.toString(true));

//		try {
//			try (OutputStream f = new FileOutputStream("target/deployments/test1.war"){
//                webArchive.writeTo(f, Formatters.VERBOSE);
//            }
//		} catch (IOException e) {
//		}
		return webArchive;
	}

	@Test
	public void should_be_deployed() {
		Assert.assertNotNull(bookEndpoint);
	}
}
