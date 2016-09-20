package simple.core.orm.hibernate;

import java.util.Date;
import java.util.List;

import javax.persistence.FetchType;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.ClassWriter;
import org.springframework.asm.FieldVisitor;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.asm.Type;

import com.google.common.base.CaseFormat;

public final class DomainCreator implements Opcodes {

	public static final byte[] create(String qualifiedName, String tableName,
			List<FieldDesc> fieldDescList) {
		// TODO why 0
		ClassWriter cw = new ClassWriter(0);
		cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
				qualifiedName.replaceAll("\\.", "/"), null, "java/lang/Object",
				null);

		// add table
		addTableDesc(cw, tableName);
		// add constructor
		addDefaultConstructor(cw);
		// add id
		createId(cw);

		for (FieldDesc fieldDesc : fieldDescList) {
			switch (fieldDesc.type) {
			case REF:
				createReference(cw, fieldDesc.refClass, fieldDesc.name,
						fieldDesc.columnName);
				break;
			case STRING:
				createCommonFiled(cw, String.class, fieldDesc.name,
						fieldDesc.columnName, fieldDesc.length, null, null);
				break;
			case LONG:
				createCommonFiled(cw, Long.class, fieldDesc.name,
						fieldDesc.columnName, null, fieldDesc.columnDefinition,
						null);
				break;
			case INTEGER:
				createCommonFiled(cw, Integer.class, fieldDesc.name,
						fieldDesc.columnName, null, fieldDesc.columnDefinition,
						null);
				break;
			case DOUBLE:
				createCommonFiled(cw, Double.class, fieldDesc.name,
						fieldDesc.columnName, null, fieldDesc.columnDefinition,
						null);
				break;
			case FLOAT:
				createCommonFiled(cw, Float.class, fieldDesc.name,
						fieldDesc.columnName, null, fieldDesc.columnDefinition,
						null);
				break;
			case BOOLEAN:
				createCommonFiled(cw, Boolean.class, fieldDesc.name,
						fieldDesc.columnName, null, fieldDesc.columnDefinition,
						fieldDesc.converter);
				break;
			case DATE:
				createCommonFiled(cw, Date.class, fieldDesc.name,
						fieldDesc.columnName, null, null, null);
				break;
			default:
				// you have trouble!
				break;
			}
		}

		cw.visitEnd();
		return cw.toByteArray();
	}

	private static final void addDefaultConstructor(ClassWriter cw) {
		MethodVisitor constructor = cw.visitMethod(ACC_PUBLIC, "<init>", "()V",
				null, null);
		constructor.visitCode();
		constructor.visitVarInsn(ALOAD, 0);
		constructor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object",
				"<init>", "()V", false);
		constructor.visitInsn(RETURN);
		constructor.visitMaxs(1, 1);
		constructor.visitEnd();
	}

	private static final void createCommonFiled(ClassWriter cw, Class<?> type,
			String name, String columnName, Integer length,
			String columnDefinition, Class<?> converter) {
		FieldVisitor fv = cw.visitField(Opcodes.ACC_PUBLIC, name,
				Type.getDescriptor(type), null, null);
		addColumnAnnotation(fv, columnName, length, columnDefinition);

		// @Convert(converter = BooleanToStringConverter.class)
		if (null != converter) {
			AnnotationVisitor av = fv.visitAnnotation(
					Type.getDescriptor(javax.persistence.Convert.class), true);
			av.visit("converter", Type.getType(converter));
			av.visitEnd();
		}

		fv.visitEnd();
	}

	private static final void createReference(ClassWriter cw,
			Class<?> refClass, String name, String columnName) {
		FieldVisitor fv = cw.visitField(Opcodes.ACC_PUBLIC, name,
				Type.getDescriptor(refClass), null, null);

		// add @ManyToOne
		AnnotationVisitor av = fv.visitAnnotation(
				Type.getDescriptor(javax.persistence.ManyToOne.class), true);
		av.visitEnum("fetch", Type.getDescriptor(FetchType.class), "LAZY");
		av.visitEnd();

		// add @JoinColumn
		av = fv.visitAnnotation(
				Type.getDescriptor(javax.persistence.JoinColumn.class), true);
		av.visit("name", columnName);
		av.visitEnd();

		fv.visitEnd();
	}

	private static final void createId(ClassWriter cw) {
		// create id field
		FieldVisitor fv = cw.visitField(Opcodes.ACC_PUBLIC, "id",
				Type.getDescriptor(Long.class), null, null);

		// add @Id annotation
		fv.visitAnnotation(Type.getDescriptor(javax.persistence.Id.class), true);

		// add @GeneratedValue
		AnnotationVisitor av = fv.visitAnnotation(
				Type.getDescriptor(javax.persistence.GeneratedValue.class),
				true);
		av.visit("generator", "idStrategy");
		av.visitEnd();

		// add @Column
		addColumnAnnotation(fv, "ID");

		fv.visitEnd();
	}

	private static final void addColumnAnnotation(FieldVisitor fv, String name) {
		addColumnAnnotation(fv, name, null, null);
	}

	private static final void addColumnAnnotation(FieldVisitor fv, String name,
			Integer length, String columnDefinition) {
		AnnotationVisitor av = fv.visitAnnotation(
				Type.getDescriptor(javax.persistence.Column.class), true);
		av.visit("name", name);
		if (null != length) {
			av.visit("length", length);
		}
		if (null != columnDefinition) {
			av.visit("columnDefinition", columnDefinition);
		}
		av.visitEnd();
	}

	private static final void addTableDesc(ClassWriter cw, String tableName) {
		String seqName = "SEQ_" + tableName;
		cw.visitAnnotation(Type.getDescriptor(javax.persistence.Entity.class),
				true);

		AnnotationVisitor av = cw.visitAnnotation(
				Type.getDescriptor(javax.persistence.Table.class), true);
		av.visit("name", tableName);
		av.visitEnd();

		av = cw.visitAnnotation(
				Type.getDescriptor(javax.persistence.SequenceGenerator.class),
				true);
		av.visit("name", seqName);
		av.visit("sequenceName", seqName);
		av.visitEnd();

		av = cw.visitAnnotation(
				Type.getDescriptor(org.hibernate.annotations.GenericGenerator.class),
				true);
		av.visit("name", "idStrategy");
		av.visit("strategy", "native");

		AnnotationVisitor parametersAv = av.visitArray("parameters");

		AnnotationVisitor parameterAv = parametersAv.visitAnnotation(null,
				Type.getDescriptor(org.hibernate.annotations.Parameter.class));
		parameterAv.visit("name", "sequence");
		parameterAv.visit("value", seqName);
		parameterAv.visitEnd();

		parametersAv.visitEnd();

		av.visitEnd();

		av = cw.visitAnnotation(Type.getDescriptor(DynamicUpdate.class), true);
		av.visitEnd();
		// arrayAv.
		//
		// av.visit("strategy", "native");
		// name = "", = "", parameters = { @Parameter(name = "sequence", value =
		// "SEQ_MDM_AREA_INFO") }
	}

	public static void main(String[] args) {
		System.out.println(CaseFormat.LOWER_CAMEL.to(
				CaseFormat.UPPER_UNDERSCORE, "SomeInputAfda"));

	}

}
