package com.nativescript.sensors;
public class Vector
{
	public double x;
	public double y;
	public double z;

	public Vector(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector(Vector vector)
	{
		this.x = vector.x;
		this.y = vector.y;
		this.z = vector.z;
	}

	public void add(Vector vector)
	{
		this.x += vector.x;
		this.y += vector.y;
		this.z += vector.z;
	}

	public void multiply(double s)
	{
		this.x *= s;
		this.y *= s;
		this.z *= s;
	}

	public Vector crossProduct(Vector vector)
	{
		return new Vector(this.y * vector.z - this.z * vector.y, this.z * vector.x - this.x * vector.z, this.x * vector.y - this.y * vector.x);
	}

	public double dotProduct(Vector vector)
	{
		return this.x * vector.x + this.y * vector.y + this.z * vector.z;
	}

	public void normalize()
	{
		if (this.getLength() != 0)
		{
			this.multiply(1.0 / this.getLength());
		}
	}

	public double getLength()
	{
		return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
	}

	public double getYaw()
	{
		return Math.toDegrees(Math.atan2(this.y, this.x));
	}

	public double getPitch()
	{
		return Math.toDegrees(Math.atan2(Math.sqrt(this.x * this.x + this.y * this.y), this.z));
	}
}
