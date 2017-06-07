package heigit.ors.services.isochrones;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import org.junit.Test;

import heigit.ors.services.common.EndPointAnnotation;
import heigit.ors.services.common.ServiceTest;

@EndPointAnnotation(name = "isochrones")
public class ResultTest extends ServiceTest {

	public ResultTest() {

		addParameter("location", "8.684177,49.423034");
		addParameter("locations", "8.684177,49.423034|8.684177,49.411034");
		addParameter("preference", "fastest");
		addParameter("profile", "cycling-regular");

	}

	@Test
	public void testPolygon() {

		given()
				.param("locations", getParameter("location"))
				.param("profile", getParameter("profile"))
				.param("range", "400")
				.when()
				.get(getEndPointName())
				.then()
				.body("any { it.key == 'type' }", is(true))
				.body("any { it.key == 'features' }", is(true))
				.body("features[0].geometry.coordinates[0].size()", is(28))
				.body("features[0].properties.center.size()", is(2))
				.body("features[0].properties.center[0]", is(8.684177f))
				.body("features[0].properties.center[1]", is(49.423034f))
				.body("bbox", hasItems(8.665036f, 49.409864f, 8.695978f, 49.439162f))
				.body("features[0].type", is("Feature"))
				.body("features[0].geometry.type", is("Polygon"))
				.body("features[0].properties.group_index", is(0))
				.body("features[0].properties.value", is(400))
				.statusCode(200);
	}

	@Test
	public void testGroupIndices() {

		given()
				.param("locations", getParameter("locations"))
				.param("profile", getParameter("profile"))
				.param("range", "400")
				.when()
				.get(getEndPointName())
				.then()
				.body("any { it.key == 'type' }", is(true))
				.body("any { it.key == 'features' }", is(true))
				.body("features.size()", is(2))
				.body("features[0].properties.group_index", is(0))
				.body("features[1].properties.group_index", is(1))
				.statusCode(200);
	}

	@Test
	public void testUnknownLocation() {

		given()
				.param("locations", "-18.215332,45.79817")
				.param("profile", getParameter("profile"))
				.param("range", "400")
				.when()
				.get(getEndPointName())
				.then()
				.statusCode(500)
				.body("error.code", is(399));
	}

	@Test
	public void testReachfactorAndArea() {

		given()
				.param("locations", getParameter("location"))
				.param("profile", getParameter("profile"))
				.param("range", "400")
				.param("attributes", "reachfactor|area")
				.when()
				.get(getEndPointName())
				.then()
				.body("any { it.key == 'type' }", is(true))
				.body("any { it.key == 'features' }", is(true))
				.body("features[0].properties.area", is(12696907.09f))
				.body("features[0].properties.reachfactor", is(0.1309f))
				.statusCode(200);
	}

	@Test
	public void testIntersections() {

		given()
				.param("locations", getParameter("locations"))
				.param("profile", getParameter("profile"))
				.param("range", "400")
				.param("attributes", "reachfactor|area")
				.param("intersections", "true")
				.when()
				.get(getEndPointName())
				.then()
				.body("any { it.key == 'type' }", is(true))
				.body("any { it.key == 'features' }", is(true))
				.body("features.size()", is(3))
				.body("features[0].type", is("Feature"))
				.body("features[0].geometry.type", is("Polygon"))
				.body("features[1].type", is("Feature"))
				.body("features[1].geometry.type", is("Polygon"))
				.body("features[2].type", is("Feature"))
				.body("features[2].geometry.type", is("Polygon"))
				.body("features[2].geometry.coordinates[0].size()", is(25))
				.body("features[2].properties.contours.size()", is(2))
				.body("features[2].properties.containsKey('area')", is(true))
				.body("features[2].properties.area", is(4807403.57f))
				.body("features[2].properties.contours[0][0]", is(0))
				.body("features[2].properties.contours[0][1]", is(0))
				.body("features[2].properties.contours[1][0]", is(1))
				.body("features[2].properties.contours[1][1]", is(0))
				.statusCode(200);
	}

}
