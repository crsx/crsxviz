package crsxviz.tests;

import static org.junit.Assert.assertEquals;

import java.sql.Time;
import java.util.Date;

import org.junit.Test;

import persistence.DaoException;
import persistence.PrimaryKey;
import persistence.impl.Property;

public class PropertyTest {

	@Test
	public void newPropertyCreatedWithoutFault() throws DaoException {
		new Property("id", int.class, true, false, TestBean.class );
	}
	
	@Test(expected=DaoException.class)
	public void improperArgumentsGivenToProperty() throws DaoException {
		new Property("id", String.class, true, false, TestBean.class);
	}
	
	@Test
	public void correctPropertiesDerivedFromClass() throws DaoException {
		Property props[] = Property.findProperties(TestBean.class, true);
		assertEquals("id", props[0].getName());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void noSuchProperty() throws DaoException {
		Property props[] = Property.findProperties(TestBean.class, true);
		assertEquals("id", props[0].getName());
		
		Property.propertyForName(props, "car");
	}
	
	@Test
	public void retrieveCorrectPropertyType() throws DaoException {
		Property props[] = Property.findProperties(TestBean.class, true);
		assertEquals(new Integer(0), props[0].defaultValue());
		assertEquals(Boolean.FALSE, props[2].defaultValue());
		assertEquals(null, props[3].defaultValue());
		assertEquals(new Double(0), props[4].defaultValue());
		assertEquals(new Float(0), props[5].defaultValue());
		assertEquals(new Long(0), props[1].defaultValue());
	}
	
	@Test
	public void isItNullable() throws DaoException {
		Property props[] = Property.findProperties(TestBean.class, true);
		assertEquals(false, props[0].isNullable());
		assertEquals(false, props[2].isNullable());
		assertEquals(true, props[3].isNullable());
		assertEquals(false, props[4].isNullable());
		assertEquals(false, props[5].isNullable());
		assertEquals(false, props[1].isNullable());
	}
	
	@Test
	public void isItAnInstanceOfWhatItShouldBe() throws DaoException {
		Property props[] = Property.findProperties(TestBean.class, true);
		assertEquals(true, props[0].isInstance(1));
		assertEquals(false, props[2].isInstance(0l));
		assertEquals(false, props[3].isInstance(0.0d));
		assertEquals(false, props[4].isInstance(0.0f));
		assertEquals(false, props[5].isInstance(true));
		assertEquals(false, props[1].isInstance(new String()));
	}
	
	
	@PrimaryKey("id")
	protected class TestBean {
		private int id;
		private String name;
		private long bigNum;
		private float floatNum;
		private double doubleNum;
		private boolean bool;
		private Date date1;
		private Time time;
		
		public TestBean() { }
		
		public TestBean(int id, String name, long bigNum, float floatNum,
				double doubleNum, boolean bool, Date date1, Time time) {
			super();
			this.id = id;
			this.name = name;
			this.bigNum = bigNum;
			this.floatNum = floatNum;
			this.doubleNum = doubleNum;
			this.bool = bool;
			this.date1 = date1;
			this.time = time;
		}


		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public long getBigNum() {
			return bigNum;
		}
		public void setBigNum(long bigNum) {
			this.bigNum = bigNum;
		}
		public float getFloatNum() {
			return floatNum;
		}
		public void setFloatNum(float floatNum) {
			this.floatNum = floatNum;
		}
		public double getDoubleNum() {
			return doubleNum;
		}
		public void setDoubleNum(double doubleNum) {
			this.doubleNum = doubleNum;
		}
		public boolean isBool() {
			return bool;
		}
		public void setBool(boolean bool) {
			this.bool = bool;
		}

		public Date getDate1() {
			return date1;
		}

		public void setDate1(Date date1) {
			this.date1 = date1;
		}

		public Time getTime() {
			return time;
		}

		public void setTime(Time time) {
			this.time = time;
		}
		
	}
}
