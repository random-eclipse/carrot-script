package worker.tester

import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import java.lang.invoke.MethodHandles
import org.slf4j.LoggerFactory
import javax.xml.parsers.DocumentBuilderFactory
import java.io.File
import javax.xml.bind.JAXBContext
import org.apache.maven.model.Model
import javax.xml.validation.SchemaFactory
import javax.xml.XMLConstants
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.apache.maven.model.Dependency

object MavenXML {

  val logger = LoggerFactory.getLogger( "here" )

  def test() = {}

  def main( args : Array[ String ] ) : Unit = {

    logger.info( "init" )

    val pomXml = new File( "pom.xml" ).getAbsoluteFile;

    val pomXsd = new File( "maven-4.0.0.xsd" ).getAbsoluteFile;

    logger.info( s"xml=${pomXml}" )

    val dbf = DocumentBuilderFactory.newInstance

    val db = dbf.newDocumentBuilder

    val document = db.parse( pomXml )

    val jc = JAXBContext.newInstance( classOf[ Model ] )

    val binder = jc.createBinder

    val node = binder.unmarshal( document, classOf[ Model ] )

    val model = node.getValue

    val dep = new Dependency()
    dep.setGroupId( "groupId" )
    dep.setArtifactId( "artifactId" )
    dep.setVersion( "1.1.1" )

    model.getDependencies().add( dep )

    logger.info( s"model=${model}" )

    //    model.setVersion("2.2.2")

    binder.updateXML( node )

    val tf = TransformerFactory.newInstance();
    val t = tf.newTransformer();
    t.transform( new DOMSource( document ), new StreamResult( System.out ) );

  }

}
