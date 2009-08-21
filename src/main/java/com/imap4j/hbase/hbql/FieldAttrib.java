package com.imap4j.hbase.hbql;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 19, 2009
 * Time: 6:07:31 PM
 */
public class FieldAttrib {

    private enum Type {

        BooleanType(Boolean.TYPE),
        ByteType(Byte.TYPE),
        CharType(Character.TYPE),
        ShortType(Short.TYPE),
        IntegerType(Integer.TYPE),
        LongType(Long.TYPE),
        FloatType(Float.TYPE),
        DoubleType(Double.TYPE),
        ObjectType(Object.class);

        private final Class clazz;

        private Type(final Class clazz) {
            this.clazz = clazz;
        }

        private Class getClazz() {
            return clazz;
        }

        private static Type getType(final Field field) throws PersistException {

            final Class fieldClass = field.getType();

            final Class<?> clazz = fieldClass.isArray() ? fieldClass.getComponentType() : fieldClass;

            if (!clazz.isPrimitive()) {
                return ObjectType;
            }
            else {
                for (final Type type : values())
                    if (clazz == type.getClazz())
                        return type;
            }

            throw new PersistException("Not dealing with type: " + clazz);
        }
    }

    private final Field field;
    private final Type type;
    private final String family;
    private final String column;
    private final String lookup;
    private final boolean mapKeysAsColumns;

    private Method lookupMethod = null;


    public FieldAttrib(final Class enclosingClass, final Field field, final Column column) throws PersistException {

        this.field = field;
        this.type = Type.getType(this.field);

        this.family = column.family();
        this.column = column.column().length() > 0 ? column.column() : this.getField().getName();
        this.lookup = column.lookup();
        this.mapKeysAsColumns = column.mapKeysAsColumns();

        try {
            if (this.isLookupAttrib()) {
                this.lookupMethod = enclosingClass.getDeclaredMethod(this.lookup);

                // Check return type and args of lookup method
                final Class<?> retClazz = this.getLookupMethod().getReturnType();

                if (!(retClazz.isArray() && retClazz.getComponentType() == Byte.TYPE))
                    throw new PersistException(enclosingClass.getName() + "." + this.lookup + "()"
                                               + " does not have a return type of byte[]");
            }
        }
        catch (NoSuchMethodException e) {
            throw new PersistException("Missing method " + enclosingClass.getName() + "." + this.lookup + "()");
        }

    }

    @Override
    public String toString() {
        return this.getField().getDeclaringClass() + "." + this.getField().getName();
    }

    public Type getComponentType() {
        return type;
    }

    private Method getLookupMethod() {
        return lookupMethod;
    }

    public boolean isLookupAttrib() {
        return this.lookup.length() > 0;
    }

    public String getFamily() {
        return this.family;
    }

    public String getColumn() {
        return column;
    }

    public Field getField() {
        return field;
    }

    public boolean isMapKeysAsColumns() {
        return this.mapKeysAsColumns;
    }

    public byte[] invokeLookupMethod(final Object parent) throws PersistException {
        try {
            return (byte[])this.getLookupMethod().invoke(parent);
        }
        catch (IllegalAccessException e) {
            throw new PersistException("Error getting value of " + this.getField().getName());
        }
        catch (InvocationTargetException e) {
            throw new PersistException("Error getting value of " + this.getField().getName());
        }
    }

    public byte[] asBytes(final Object obj) throws IOException, PersistException {
        final Class clazz = obj.getClass();

        if (clazz.isArray())
            return this.getArrayasBytes(obj);
        else
            return this.getScalarAsBytes(obj);
    }

    private byte[] getScalarAsBytes(final Object obj) throws IOException, PersistException {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);

        switch (this.getComponentType()) {

            case BooleanType:
                oos.writeBoolean((Boolean)obj);
                break;

            case ByteType:
                oos.writeByte((Byte)obj);
                break;

            case CharType:
                oos.writeByte((Byte)obj);
                break;

            case ShortType:
                oos.writeShort((Short)obj);
                break;

            case IntegerType:
                oos.writeInt((Integer)obj);
                break;

            case LongType:
                oos.writeLong((Long)obj);
                break;

            case FloatType:
                oos.writeFloat((Float)obj);
                break;

            case DoubleType:
                oos.writeDouble((Double)obj);
                break;

            case ObjectType:
                oos.writeObject(obj);
                break;
        }

        oos.flush();
        return baos.toByteArray();

    }

    private byte[] getArrayasBytes(final Object obj) throws IOException, PersistException {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);

        switch (this.getComponentType()) {

            case BooleanType: {
                final boolean[] val = (boolean[])obj;
                for (int i = 0; i < val.length; i++) oos.writeBoolean(val[i]);
                break;
            }

            case ByteType: {
                final byte[] val = (byte[])obj;
                for (int i = 0; i < val.length; i++) oos.write(val[i]);
                break;
            }

            case CharType: {
                final char[] val = (char[])obj;
                for (int i = 0; i < val.length; i++) oos.write(val[i]);
                break;
            }

            case ShortType: {
                final short[] val = (short[])obj;
                for (int i = 0; i < val.length; i++) oos.writeShort(val[i]);
                break;
            }

            case IntegerType: {
                final int[] val = (int[])obj;
                for (int i = 0; i < val.length; i++) oos.writeInt(val[i]);
                break;
            }

            case LongType: {
                final long[] val = (long[])obj;
                for (int i = 0; i < val.length; i++) oos.writeLong(val[i]);
                break;
            }

            case FloatType: {
                final float[] val = (float[])obj;
                for (int i = 0; i < val.length; i++) oos.writeFloat(val[i]);
                break;
            }

            case DoubleType: {
                final double[] val = (double[])obj;
                for (int i = 0; i < val.length; i++) oos.writeDouble(val[i]);
                break;
            }

            case ObjectType: {
                final Object[] val = (Object[])obj;
                for (int i = 0; i < val.length; i++) oos.writeObject(val[i]);
                break;
            }
        }

        oos.flush();
        return baos.toByteArray();

    }

}