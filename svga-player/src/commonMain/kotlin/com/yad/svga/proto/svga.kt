@file:OptIn(pbandk.PublicForGeneratedCode::class)

package com.yad.svga.proto

import pbandk.*

// ============================================================================
// MovieParams - 动画参数
// ============================================================================

@pbandk.Export
public data class MovieParams(
    /** 画布宽 */
    val viewBoxWidth: Float? = null,
    /** 画布高 */
    val viewBoxHeight: Float? = null,
    /** 动画每秒播放帧数 */
    val fps: Int? = null,
    /** 动画总帧数 */
    val frames: Int? = null,
    override val unknownFields: Map<Int, UnknownField> = emptyMap()
) : Message {
    override operator fun plus(other: Message?): MovieParams = protoMergeImpl(other)
    override val descriptor: MessageDescriptor<MovieParams> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }

    public companion object : Message.Companion<MovieParams> {
        public val defaultInstance: MovieParams by lazy { MovieParams() }
        override fun decodeWith(u: MessageDecoder): MovieParams = MovieParams.decodeWithImpl(u)

        override val descriptor: MessageDescriptor<MovieParams> by lazy {
            val fieldsList = ArrayList<FieldDescriptor<MovieParams, *>>(4)
            fieldsList.apply {
                add(FieldDescriptor(
                    messageDescriptor = this@Companion::descriptor,
                    name = "viewBoxWidth", number = 1,
                    type = FieldDescriptor.Type.Primitive.Float(hasPresence = true),
                    jsonName = "viewBoxWidth",
                    value = MovieParams::viewBoxWidth
                ))
                add(FieldDescriptor(
                    messageDescriptor = this@Companion::descriptor,
                    name = "viewBoxHeight", number = 2,
                    type = FieldDescriptor.Type.Primitive.Float(hasPresence = true),
                    jsonName = "viewBoxHeight",
                    value = MovieParams::viewBoxHeight
                ))
                add(FieldDescriptor(
                    messageDescriptor = this@Companion::descriptor,
                    name = "fps", number = 3,
                    type = FieldDescriptor.Type.Primitive.Int32(hasPresence = true),
                    jsonName = "fps",
                    value = MovieParams::fps
                ))
                add(FieldDescriptor(
                    messageDescriptor = this@Companion::descriptor,
                    name = "frames", number = 4,
                    type = FieldDescriptor.Type.Primitive.Int32(hasPresence = true),
                    jsonName = "frames",
                    value = MovieParams::frames
                ))
            }
            MessageDescriptor(
                fullName = "com.yad.svga.proto.MovieParams",
                messageClass = MovieParams::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

// ============================================================================
// Layout - 布局
// ============================================================================

@pbandk.Export
public data class Layout(
    val x: Float? = null,
    val y: Float? = null,
    val width: Float? = null,
    val height: Float? = null,
    override val unknownFields: Map<Int, UnknownField> = emptyMap()
) : Message {
    override operator fun plus(other: Message?): Layout = protoMergeImpl(other)
    override val descriptor: MessageDescriptor<Layout> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }

    public companion object : Message.Companion<Layout> {
        public val defaultInstance: Layout by lazy { Layout() }
        override fun decodeWith(u: MessageDecoder): Layout = Layout.decodeWithImpl(u)

        override val descriptor: MessageDescriptor<Layout> by lazy {
            val fieldsList = ArrayList<FieldDescriptor<Layout, *>>(4)
            fieldsList.apply {
                add(FieldDescriptor(
                    messageDescriptor = this@Companion::descriptor,
                    name = "x", number = 1,
                    type = FieldDescriptor.Type.Primitive.Float(hasPresence = true),
                    jsonName = "x", value = Layout::x
                ))
                add(FieldDescriptor(
                    messageDescriptor = this@Companion::descriptor,
                    name = "y", number = 2,
                    type = FieldDescriptor.Type.Primitive.Float(hasPresence = true),
                    jsonName = "y", value = Layout::y
                ))
                add(FieldDescriptor(
                    messageDescriptor = this@Companion::descriptor,
                    name = "width", number = 3,
                    type = FieldDescriptor.Type.Primitive.Float(hasPresence = true),
                    jsonName = "width", value = Layout::width
                ))
                add(FieldDescriptor(
                    messageDescriptor = this@Companion::descriptor,
                    name = "height", number = 4,
                    type = FieldDescriptor.Type.Primitive.Float(hasPresence = true),
                    jsonName = "height", value = Layout::height
                ))
            }
            MessageDescriptor(
                fullName = "com.yad.svga.proto.Layout",
                messageClass = Layout::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

// ============================================================================
// Transform - 2D 仿射变换矩阵
// ============================================================================

@pbandk.Export
public data class Transform(
    val a: Float? = null,
    val b: Float? = null,
    val c: Float? = null,
    val d: Float? = null,
    val tx: Float? = null,
    val ty: Float? = null,
    override val unknownFields: Map<Int, UnknownField> = emptyMap()
) : Message {
    override operator fun plus(other: Message?): Transform = protoMergeImpl(other)
    override val descriptor: MessageDescriptor<Transform> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }

    public companion object : Message.Companion<Transform> {
        public val defaultInstance: Transform by lazy { Transform() }
        override fun decodeWith(u: MessageDecoder): Transform = Transform.decodeWithImpl(u)

        override val descriptor: MessageDescriptor<Transform> by lazy {
            val fieldsList = ArrayList<FieldDescriptor<Transform, *>>(6)
            fieldsList.apply {
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "a", number = 1, type = FieldDescriptor.Type.Primitive.Float(hasPresence = true), jsonName = "a", value = Transform::a))
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "b", number = 2, type = FieldDescriptor.Type.Primitive.Float(hasPresence = true), jsonName = "b", value = Transform::b))
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "c", number = 3, type = FieldDescriptor.Type.Primitive.Float(hasPresence = true), jsonName = "c", value = Transform::c))
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "d", number = 4, type = FieldDescriptor.Type.Primitive.Float(hasPresence = true), jsonName = "d", value = Transform::d))
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "tx", number = 5, type = FieldDescriptor.Type.Primitive.Float(hasPresence = true), jsonName = "tx", value = Transform::tx))
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "ty", number = 6, type = FieldDescriptor.Type.Primitive.Float(hasPresence = true), jsonName = "ty", value = Transform::ty))
            }
            MessageDescriptor(
                fullName = "com.yad.svga.proto.Transform",
                messageClass = Transform::class,
                messageCompanion = this,
                fields = fieldsList
            )
        }
    }
}

// ============================================================================
// ShapeEntity - 矢量形状
// ============================================================================

@pbandk.Export
public data class ShapeEntity(
    /** 矢量类型 */
    val type: ShapeType? = null,
    /** 路径参数 */
    val shape: ShapeArgs? = null,
    /** 矩形参数 */
    val rect: RectArgs? = null,
    /** 椭圆参数 */
    val ellipse: EllipseArgs? = null,
    /** 渲染参数 */
    val styles: ShapeStyle? = null,
    /** 矢量图层 2D 变换矩阵 */
    val transform: Transform? = null,
    override val unknownFields: Map<Int, UnknownField> = emptyMap()
) : Message {
    override operator fun plus(other: Message?): ShapeEntity = protoMergeImpl(other)
    override val descriptor: MessageDescriptor<ShapeEntity> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }

    public sealed class ShapeType(override val value: Int, override val name: String? = null) : Message.Enum {
        override fun equals(other: Any?): Boolean = other is ShapeType && other.value == value
        override fun hashCode(): Int = value.hashCode()
        override fun toString(): String = "ShapeEntity.ShapeType.${name ?: "UNRECOGNIZED"}(value=$value)"

        public object SHAPE : ShapeType(0, "SHAPE")
        public object RECT : ShapeType(1, "RECT")
        public object ELLIPSE : ShapeType(2, "ELLIPSE")
        public object KEEP : ShapeType(3, "KEEP")
        public class UNRECOGNIZED(value: Int) : ShapeType(value)

        public companion object : Message.Enum.Companion<ShapeType> {
            public val values: List<ShapeType> by lazy { listOf(SHAPE, RECT, ELLIPSE, KEEP) }
            override fun fromValue(value: Int): ShapeType = values.firstOrNull { it.value == value } ?: UNRECOGNIZED(value)
            override fun fromName(name: String): ShapeType = values.firstOrNull { it.name == name } ?: throw IllegalArgumentException("No ShapeType with name: $name")
        }
    }

    /** SVG 路径参数 */
    @pbandk.Export
    public data class ShapeArgs(
        /** SVG 路径 */
        val d: String? = null,
        override val unknownFields: Map<Int, UnknownField> = emptyMap()
    ) : Message {
        override operator fun plus(other: Message?): ShapeArgs = protoMergeImpl(other)
        override val descriptor: MessageDescriptor<ShapeArgs> get() = Companion.descriptor
        override val protoSize: Int by lazy { super.protoSize }

        public companion object : Message.Companion<ShapeArgs> {
            public val defaultInstance: ShapeArgs by lazy { ShapeArgs() }
            override fun decodeWith(u: MessageDecoder): ShapeArgs = ShapeArgs.decodeWithImpl(u)

            override val descriptor: MessageDescriptor<ShapeArgs> by lazy {
                val fieldsList = ArrayList<FieldDescriptor<ShapeArgs, *>>(1)
                fieldsList.apply {
                    add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "d", number = 1, type = FieldDescriptor.Type.Primitive.String(hasPresence = true), jsonName = "d", value = ShapeArgs::d))
                }
                MessageDescriptor(fullName = "com.yad.svga.proto.ShapeEntity.ShapeArgs", messageClass = ShapeArgs::class, messageCompanion = this, fields = fieldsList)
            }
        }
    }

    /** 矩形参数 */
    @pbandk.Export
    public data class RectArgs(
        val x: Float? = null,
        val y: Float? = null,
        val width: Float? = null,
        val height: Float? = null,
        /** 圆角半径 */
        val cornerRadius: Float? = null,
        override val unknownFields: Map<Int, UnknownField> = emptyMap()
    ) : Message {
        override operator fun plus(other: Message?): RectArgs = protoMergeImpl(other)
        override val descriptor: MessageDescriptor<RectArgs> get() = Companion.descriptor
        override val protoSize: Int by lazy { super.protoSize }

        public companion object : Message.Companion<RectArgs> {
            public val defaultInstance: RectArgs by lazy { RectArgs() }
            override fun decodeWith(u: MessageDecoder): RectArgs = RectArgs.decodeWithImpl(u)

            override val descriptor: MessageDescriptor<RectArgs> by lazy {
                val fieldsList = ArrayList<FieldDescriptor<RectArgs, *>>(5)
                fieldsList.apply {
                    add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "x", number = 1, type = FieldDescriptor.Type.Primitive.Float(hasPresence = true), jsonName = "x", value = RectArgs::x))
                    add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "y", number = 2, type = FieldDescriptor.Type.Primitive.Float(hasPresence = true), jsonName = "y", value = RectArgs::y))
                    add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "width", number = 3, type = FieldDescriptor.Type.Primitive.Float(hasPresence = true), jsonName = "width", value = RectArgs::width))
                    add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "height", number = 4, type = FieldDescriptor.Type.Primitive.Float(hasPresence = true), jsonName = "height", value = RectArgs::height))
                    add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "cornerRadius", number = 5, type = FieldDescriptor.Type.Primitive.Float(hasPresence = true), jsonName = "cornerRadius", value = RectArgs::cornerRadius))
                }
                MessageDescriptor(fullName = "com.yad.svga.proto.ShapeEntity.RectArgs", messageClass = RectArgs::class, messageCompanion = this, fields = fieldsList)
            }
        }
    }

    /** 椭圆参数 */
    @pbandk.Export
    public data class EllipseArgs(
        /** 圆中心点 X */
        val x: Float? = null,
        /** 圆中心点 Y */
        val y: Float? = null,
        /** 横向半径 */
        val radiusX: Float? = null,
        /** 纵向半径 */
        val radiusY: Float? = null,
        override val unknownFields: Map<Int, UnknownField> = emptyMap()
    ) : Message {
        override operator fun plus(other: Message?): EllipseArgs = protoMergeImpl(other)
        override val descriptor: MessageDescriptor<EllipseArgs> get() = Companion.descriptor
        override val protoSize: Int by lazy { super.protoSize }

        public companion object : Message.Companion<EllipseArgs> {
            public val defaultInstance: EllipseArgs by lazy { EllipseArgs() }
            override fun decodeWith(u: MessageDecoder): EllipseArgs = EllipseArgs.decodeWithImpl(u)

            override val descriptor: MessageDescriptor<EllipseArgs> by lazy {
                val fieldsList = ArrayList<FieldDescriptor<EllipseArgs, *>>(4)
                fieldsList.apply {
                    add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "x", number = 1, type = FieldDescriptor.Type.Primitive.Float(hasPresence = true), jsonName = "x", value = EllipseArgs::x))
                    add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "y", number = 2, type = FieldDescriptor.Type.Primitive.Float(hasPresence = true), jsonName = "y", value = EllipseArgs::y))
                    add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "radiusX", number = 3, type = FieldDescriptor.Type.Primitive.Float(hasPresence = true), jsonName = "radiusX", value = EllipseArgs::radiusX))
                    add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "radiusY", number = 4, type = FieldDescriptor.Type.Primitive.Float(hasPresence = true), jsonName = "radiusY", value = EllipseArgs::radiusY))
                }
                MessageDescriptor(fullName = "com.yad.svga.proto.ShapeEntity.EllipseArgs", messageClass = EllipseArgs::class, messageCompanion = this, fields = fieldsList)
            }
        }
    }

    /** RGBA 颜色 */
    @pbandk.Export
    public data class RGBAColor(
        val r: Float? = null,
        val g: Float? = null,
        val b: Float? = null,
        val a: Float? = null,
        override val unknownFields: Map<Int, UnknownField> = emptyMap()
    ) : Message {
        override operator fun plus(other: Message?): RGBAColor = protoMergeImpl(other)
        override val descriptor: MessageDescriptor<RGBAColor> get() = Companion.descriptor
        override val protoSize: Int by lazy { super.protoSize }

        public companion object : Message.Companion<RGBAColor> {
            public val defaultInstance: RGBAColor by lazy { RGBAColor() }
            override fun decodeWith(u: MessageDecoder): RGBAColor = RGBAColor.decodeWithImpl(u)

            override val descriptor: MessageDescriptor<RGBAColor> by lazy {
                val fieldsList = ArrayList<FieldDescriptor<RGBAColor, *>>(4)
                fieldsList.apply {
                    add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "r", number = 1, type = FieldDescriptor.Type.Primitive.Float(hasPresence = true), jsonName = "r", value = RGBAColor::r))
                    add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "g", number = 2, type = FieldDescriptor.Type.Primitive.Float(hasPresence = true), jsonName = "g", value = RGBAColor::g))
                    add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "b", number = 3, type = FieldDescriptor.Type.Primitive.Float(hasPresence = true), jsonName = "b", value = RGBAColor::b))
                    add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "a", number = 4, type = FieldDescriptor.Type.Primitive.Float(hasPresence = true), jsonName = "a", value = RGBAColor::a))
                }
                MessageDescriptor(fullName = "com.yad.svga.proto.ShapeEntity.RGBAColor", messageClass = RGBAColor::class, messageCompanion = this, fields = fieldsList)
            }
        }
    }

    /** 渲染样式 */
    @pbandk.Export
    public data class ShapeStyle(
        /** 填充色 */
        val fill: RGBAColor? = null,
        /** 描边色 */
        val stroke: RGBAColor? = null,
        /** 描边宽 */
        val strokeWidth: Float? = null,
        /** 线段端点样式 */
        val lineCap: LineCap? = null,
        /** 线段连接样式 */
        val lineJoin: LineJoin? = null,
        /** 尖角限制 */
        val miterLimit: Float? = null,
        /** 虚线参数 Dash */
        val lineDashI: Float? = null,
        /** 虚线参数 Gap */
        val lineDashII: Float? = null,
        /** 虚线参数 Offset */
        val lineDashIII: Float? = null,
        override val unknownFields: Map<Int, UnknownField> = emptyMap()
    ) : Message {
        override operator fun plus(other: Message?): ShapeStyle = protoMergeImpl(other)
        override val descriptor: MessageDescriptor<ShapeStyle> get() = Companion.descriptor
        override val protoSize: Int by lazy { super.protoSize }

        public sealed class LineCap(override val value: Int, override val name: String? = null) : Message.Enum {
            override fun equals(other: Any?): Boolean = other is LineCap && other.value == value
            override fun hashCode(): Int = value.hashCode()
            override fun toString(): String = "ShapeStyle.LineCap.${name ?: "UNRECOGNIZED"}(value=$value)"

            public object BUTT : LineCap(0, "LineCap_BUTT")
            public object ROUND : LineCap(1, "LineCap_ROUND")
            public object SQUARE : LineCap(2, "LineCap_SQUARE")
            public class UNRECOGNIZED(value: Int) : LineCap(value)

            public companion object : Message.Enum.Companion<LineCap> {
                public val values: List<LineCap> by lazy { listOf(BUTT, ROUND, SQUARE) }
                override fun fromValue(value: Int): LineCap = values.firstOrNull { it.value == value } ?: UNRECOGNIZED(value)
                override fun fromName(name: String): LineCap = values.firstOrNull { it.name == name } ?: throw IllegalArgumentException("No LineCap with name: $name")
            }
        }

        public sealed class LineJoin(override val value: Int, override val name: String? = null) : Message.Enum {
            override fun equals(other: Any?): Boolean = other is LineJoin && other.value == value
            override fun hashCode(): Int = value.hashCode()
            override fun toString(): String = "ShapeStyle.LineJoin.${name ?: "UNRECOGNIZED"}(value=$value)"

            public object MITER : LineJoin(0, "LineJoin_MITER")
            public object ROUND : LineJoin(1, "LineJoin_ROUND")
            public object BEVEL : LineJoin(2, "LineJoin_BEVEL")
            public class UNRECOGNIZED(value: Int) : LineJoin(value)

            public companion object : Message.Enum.Companion<LineJoin> {
                public val values: List<LineJoin> by lazy { listOf(MITER, ROUND, BEVEL) }
                override fun fromValue(value: Int): LineJoin = values.firstOrNull { it.value == value } ?: UNRECOGNIZED(value)
                override fun fromName(name: String): LineJoin = values.firstOrNull { it.name == name } ?: throw IllegalArgumentException("No LineJoin with name: $name")
            }
        }

        public companion object : Message.Companion<ShapeStyle> {
            public val defaultInstance: ShapeStyle by lazy { ShapeStyle() }
            override fun decodeWith(u: MessageDecoder): ShapeStyle = ShapeStyle.decodeWithImpl(u)

            override val descriptor: MessageDescriptor<ShapeStyle> by lazy {
                val fieldsList = ArrayList<FieldDescriptor<ShapeStyle, *>>(9)
                fieldsList.apply {
                    add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "fill", number = 1, type = FieldDescriptor.Type.Message(messageCompanion = RGBAColor.Companion), jsonName = "fill", value = ShapeStyle::fill))
                    add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "stroke", number = 2, type = FieldDescriptor.Type.Message(messageCompanion = RGBAColor.Companion), jsonName = "stroke", value = ShapeStyle::stroke))
                    add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "strokeWidth", number = 3, type = FieldDescriptor.Type.Primitive.Float(hasPresence = true), jsonName = "strokeWidth", value = ShapeStyle::strokeWidth))
                    add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "lineCap", number = 4, type = FieldDescriptor.Type.Enum(enumCompanion = LineCap.Companion, hasPresence = true), jsonName = "lineCap", value = ShapeStyle::lineCap))
                    add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "lineJoin", number = 5, type = FieldDescriptor.Type.Enum(enumCompanion = LineJoin.Companion, hasPresence = true), jsonName = "lineJoin", value = ShapeStyle::lineJoin))
                    add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "miterLimit", number = 6, type = FieldDescriptor.Type.Primitive.Float(hasPresence = true), jsonName = "miterLimit", value = ShapeStyle::miterLimit))
                    add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "lineDashI", number = 7, type = FieldDescriptor.Type.Primitive.Float(hasPresence = true), jsonName = "lineDashI", value = ShapeStyle::lineDashI))
                    add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "lineDashII", number = 8, type = FieldDescriptor.Type.Primitive.Float(hasPresence = true), jsonName = "lineDashII", value = ShapeStyle::lineDashII))
                    add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "lineDashIII", number = 9, type = FieldDescriptor.Type.Primitive.Float(hasPresence = true), jsonName = "lineDashIII", value = ShapeStyle::lineDashIII))
                }
                MessageDescriptor(fullName = "com.yad.svga.proto.ShapeEntity.ShapeStyle", messageClass = ShapeStyle::class, messageCompanion = this, fields = fieldsList)
            }
        }
    }

    public companion object : Message.Companion<ShapeEntity> {
        public val defaultInstance: ShapeEntity by lazy { ShapeEntity() }
        override fun decodeWith(u: MessageDecoder): ShapeEntity = ShapeEntity.decodeWithImpl(u)

        override val descriptor: MessageDescriptor<ShapeEntity> by lazy {
            val fieldsList = ArrayList<FieldDescriptor<ShapeEntity, *>>(6)
            fieldsList.apply {
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "type", number = 1, type = FieldDescriptor.Type.Enum(enumCompanion = ShapeType.Companion, hasPresence = true), jsonName = "type", value = ShapeEntity::type))
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "shape", number = 2, type = FieldDescriptor.Type.Message(messageCompanion = ShapeArgs.Companion), jsonName = "shape", value = ShapeEntity::shape))
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "rect", number = 3, type = FieldDescriptor.Type.Message(messageCompanion = RectArgs.Companion), jsonName = "rect", value = ShapeEntity::rect))
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "ellipse", number = 4, type = FieldDescriptor.Type.Message(messageCompanion = EllipseArgs.Companion), jsonName = "ellipse", value = ShapeEntity::ellipse))
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "styles", number = 10, type = FieldDescriptor.Type.Message(messageCompanion = ShapeStyle.Companion), jsonName = "styles", value = ShapeEntity::styles))
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "transform", number = 11, type = FieldDescriptor.Type.Message(messageCompanion = Transform.Companion), jsonName = "transform", value = ShapeEntity::transform))
            }
            MessageDescriptor(fullName = "com.yad.svga.proto.ShapeEntity", messageClass = ShapeEntity::class, messageCompanion = this, fields = fieldsList)
        }
    }
}

// ============================================================================
// FrameEntity - 帧
// ============================================================================

@pbandk.Export
public data class FrameEntity(
    /** 透明度 */
    val alpha: Float? = null,
    /** 初始约束大小 */
    val layout: Layout? = null,
    /** 2D 变换矩阵 */
    val transform: Transform? = null,
    /** 遮罩路径（SVG Path） */
    val clipPath: String? = null,
    /** 矢量元素列表 */
    val shapes: List<ShapeEntity> = emptyList(),
    override val unknownFields: Map<Int, UnknownField> = emptyMap()
) : Message {
    override operator fun plus(other: Message?): FrameEntity = protoMergeImpl(other)
    override val descriptor: MessageDescriptor<FrameEntity> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }

    public companion object : Message.Companion<FrameEntity> {
        public val defaultInstance: FrameEntity by lazy { FrameEntity() }
        override fun decodeWith(u: MessageDecoder): FrameEntity = FrameEntity.decodeWithImpl(u)

        override val descriptor: MessageDescriptor<FrameEntity> by lazy {
            val fieldsList = ArrayList<FieldDescriptor<FrameEntity, *>>(5)
            fieldsList.apply {
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "alpha", number = 1, type = FieldDescriptor.Type.Primitive.Float(hasPresence = true), jsonName = "alpha", value = FrameEntity::alpha))
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "layout", number = 2, type = FieldDescriptor.Type.Message(messageCompanion = Layout.Companion), jsonName = "layout", value = FrameEntity::layout))
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "transform", number = 3, type = FieldDescriptor.Type.Message(messageCompanion = Transform.Companion), jsonName = "transform", value = FrameEntity::transform))
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "clipPath", number = 4, type = FieldDescriptor.Type.Primitive.String(hasPresence = true), jsonName = "clipPath", value = FrameEntity::clipPath))
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "shapes", number = 5, type = FieldDescriptor.Type.Repeated<ShapeEntity>(valueType = FieldDescriptor.Type.Message(messageCompanion = ShapeEntity.Companion)), jsonName = "shapes", value = FrameEntity::shapes))
            }
            MessageDescriptor(fullName = "com.yad.svga.proto.FrameEntity", messageClass = FrameEntity::class, messageCompanion = this, fields = fieldsList)
        }
    }
}

// ============================================================================
// AudioEntity - 音频
// ============================================================================

@pbandk.Export
public data class AudioEntity(
    /** 音频文件名 */
    val audioKey: String? = null,
    /** 音频播放起始帧 */
    val startFrame: Int? = null,
    /** 音频播放结束帧 */
    val endFrame: Int? = null,
    /** 音频播放起始时间（相对音频长度） */
    val startTime: Int? = null,
    /** 音频总长度 */
    val totalTime: Int? = null,
    override val unknownFields: Map<Int, UnknownField> = emptyMap()
) : Message {
    override operator fun plus(other: Message?): AudioEntity = protoMergeImpl(other)
    override val descriptor: MessageDescriptor<AudioEntity> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }

    public companion object : Message.Companion<AudioEntity> {
        public val defaultInstance: AudioEntity by lazy { AudioEntity() }
        override fun decodeWith(u: MessageDecoder): AudioEntity = AudioEntity.decodeWithImpl(u)

        override val descriptor: MessageDescriptor<AudioEntity> by lazy {
            val fieldsList = ArrayList<FieldDescriptor<AudioEntity, *>>(5)
            fieldsList.apply {
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "audioKey", number = 1, type = FieldDescriptor.Type.Primitive.String(hasPresence = true), jsonName = "audioKey", value = AudioEntity::audioKey))
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "startFrame", number = 2, type = FieldDescriptor.Type.Primitive.Int32(hasPresence = true), jsonName = "startFrame", value = AudioEntity::startFrame))
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "endFrame", number = 3, type = FieldDescriptor.Type.Primitive.Int32(hasPresence = true), jsonName = "endFrame", value = AudioEntity::endFrame))
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "startTime", number = 4, type = FieldDescriptor.Type.Primitive.Int32(hasPresence = true), jsonName = "startTime", value = AudioEntity::startTime))
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "totalTime", number = 5, type = FieldDescriptor.Type.Primitive.Int32(hasPresence = true), jsonName = "totalTime", value = AudioEntity::totalTime))
            }
            MessageDescriptor(fullName = "com.yad.svga.proto.AudioEntity", messageClass = AudioEntity::class, messageCompanion = this, fields = fieldsList)
        }
    }
}

// ============================================================================
// SpriteEntity - 元件（精灵）
// ============================================================================

@pbandk.Export
public data class SpriteEntity(
    /** 位图键名，含 .vector 后缀为矢量图层，含 .matte 后缀为遮罩图层 */
    val imageKey: String? = null,
    /** 帧列表 */
    val frames: List<FrameEntity> = emptyList(),
    /** 被遮罩图层的 matteKey 对应其遮罩图层的 imageKey */
    val matteKey: String? = null,
    override val unknownFields: Map<Int, UnknownField> = emptyMap()
) : Message {
    override operator fun plus(other: Message?): SpriteEntity = protoMergeImpl(other)
    override val descriptor: MessageDescriptor<SpriteEntity> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }

    public companion object : Message.Companion<SpriteEntity> {
        public val defaultInstance: SpriteEntity by lazy { SpriteEntity() }
        override fun decodeWith(u: MessageDecoder): SpriteEntity = SpriteEntity.decodeWithImpl(u)

        override val descriptor: MessageDescriptor<SpriteEntity> by lazy {
            val fieldsList = ArrayList<FieldDescriptor<SpriteEntity, *>>(3)
            fieldsList.apply {
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "imageKey", number = 1, type = FieldDescriptor.Type.Primitive.String(hasPresence = true), jsonName = "imageKey", value = SpriteEntity::imageKey))
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "frames", number = 2, type = FieldDescriptor.Type.Repeated<FrameEntity>(valueType = FieldDescriptor.Type.Message(messageCompanion = FrameEntity.Companion)), jsonName = "frames", value = SpriteEntity::frames))
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "matteKey", number = 3, type = FieldDescriptor.Type.Primitive.String(hasPresence = true), jsonName = "matteKey", value = SpriteEntity::matteKey))
            }
            MessageDescriptor(fullName = "com.yad.svga.proto.SpriteEntity", messageClass = SpriteEntity::class, messageCompanion = this, fields = fieldsList)
        }
    }
}

// ============================================================================
// MovieEntity - 顶层动画实体
// ============================================================================

@pbandk.Export
public data class MovieEntity(
    /** SVGA 格式版本号 */
    val version: String? = null,
    /** 动画参数 */
    val params: MovieParams? = null,
    /** 位图键名 -> 位图二进制数据 */
    val images: Map<String, ByteArr> = emptyMap(),
    /** 元素列表 */
    val sprites: List<SpriteEntity> = emptyList(),
    /** 音频列表 */
    val audios: List<AudioEntity> = emptyList(),
    override val unknownFields: Map<Int, UnknownField> = emptyMap()
) : Message {
    override operator fun plus(other: Message?): MovieEntity = protoMergeImpl(other)
    override val descriptor: MessageDescriptor<MovieEntity> get() = Companion.descriptor
    override val protoSize: Int by lazy { super.protoSize }

    public companion object : Message.Companion<MovieEntity> {
        public val defaultInstance: MovieEntity by lazy { MovieEntity() }
        override fun decodeWith(u: MessageDecoder): MovieEntity = MovieEntity.decodeWithImpl(u)

        override val descriptor: MessageDescriptor<MovieEntity> by lazy {
            val fieldsList = ArrayList<FieldDescriptor<MovieEntity, *>>(5)
            fieldsList.apply {
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "version", number = 1, type = FieldDescriptor.Type.Primitive.String(hasPresence = true), jsonName = "version", value = MovieEntity::version))
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "params", number = 2, type = FieldDescriptor.Type.Message(messageCompanion = MovieParams.Companion), jsonName = "params", value = MovieEntity::params))
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "images", number = 3, type = FieldDescriptor.Type.Map<String, ByteArr>(keyType = FieldDescriptor.Type.Primitive.String(), valueType = FieldDescriptor.Type.Primitive.Bytes()), jsonName = "images", value = MovieEntity::images))
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "sprites", number = 4, type = FieldDescriptor.Type.Repeated<SpriteEntity>(valueType = FieldDescriptor.Type.Message(messageCompanion = SpriteEntity.Companion)), jsonName = "sprites", value = MovieEntity::sprites))
                add(FieldDescriptor(messageDescriptor = this@Companion::descriptor, name = "audios", number = 5, type = FieldDescriptor.Type.Repeated<AudioEntity>(valueType = FieldDescriptor.Type.Message(messageCompanion = AudioEntity.Companion)), jsonName = "audios", value = MovieEntity::audios))
            }
            MessageDescriptor(fullName = "com.yad.svga.proto.MovieEntity", messageClass = MovieEntity::class, messageCompanion = this, fields = fieldsList)
        }
    }
}

// ============================================================================
// protoMergeImpl / decodeWithImpl extension functions
// (pbandk generates these as private extensions in the same file)
// ============================================================================

// --- MovieParams ---
private fun MovieParams.protoMergeImpl(other: Message?): MovieParams {
    if (other == null || other !is MovieParams) return this
    return copy(
        viewBoxWidth = other.viewBoxWidth ?: viewBoxWidth,
        viewBoxHeight = other.viewBoxHeight ?: viewBoxHeight,
        fps = other.fps ?: fps,
        frames = other.frames ?: frames,
        unknownFields = unknownFields + other.unknownFields
    )
}

private fun MovieParams.Companion.decodeWithImpl(u: MessageDecoder): MovieParams {
    var viewBoxWidth: Float? = null
    var viewBoxHeight: Float? = null
    var fps: Int? = null
    var frames: Int? = null
    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> viewBoxWidth = _fieldValue as Float
            2 -> viewBoxHeight = _fieldValue as Float
            3 -> fps = _fieldValue as Int
            4 -> frames = _fieldValue as Int
        }
    }
    return MovieParams(viewBoxWidth, viewBoxHeight, fps, frames, unknownFields)
}

// --- Layout ---
private fun Layout.protoMergeImpl(other: Message?): Layout {
    if (other == null || other !is Layout) return this
    return copy(
        x = other.x ?: x, y = other.y ?: y,
        width = other.width ?: width, height = other.height ?: height,
        unknownFields = unknownFields + other.unknownFields
    )
}

private fun Layout.Companion.decodeWithImpl(u: MessageDecoder): Layout {
    var x: Float? = null; var y: Float? = null
    var width: Float? = null; var height: Float? = null
    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> x = _fieldValue as Float
            2 -> y = _fieldValue as Float
            3 -> width = _fieldValue as Float
            4 -> height = _fieldValue as Float
        }
    }
    return Layout(x, y, width, height, unknownFields)
}

// --- Transform ---
private fun Transform.protoMergeImpl(other: Message?): Transform {
    if (other == null || other !is Transform) return this
    return copy(
        a = other.a ?: a, b = other.b ?: b, c = other.c ?: c,
        d = other.d ?: d, tx = other.tx ?: tx, ty = other.ty ?: ty,
        unknownFields = unknownFields + other.unknownFields
    )
}

private fun Transform.Companion.decodeWithImpl(u: MessageDecoder): Transform {
    var a: Float? = null; var b: Float? = null; var c: Float? = null
    var d: Float? = null; var tx: Float? = null; var ty: Float? = null
    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> a = _fieldValue as Float
            2 -> b = _fieldValue as Float
            3 -> c = _fieldValue as Float
            4 -> d = _fieldValue as Float
            5 -> tx = _fieldValue as Float
            6 -> ty = _fieldValue as Float
        }
    }
    return Transform(a, b, c, d, tx, ty, unknownFields)
}

// --- ShapeEntity.ShapeArgs ---
private fun ShapeEntity.ShapeArgs.protoMergeImpl(other: Message?): ShapeEntity.ShapeArgs {
    if (other == null || other !is ShapeEntity.ShapeArgs) return this
    return copy(d = other.d ?: d, unknownFields = unknownFields + other.unknownFields)
}

private fun ShapeEntity.ShapeArgs.Companion.decodeWithImpl(u: MessageDecoder): ShapeEntity.ShapeArgs {
    var d: String? = null
    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) { 1 -> d = _fieldValue as String }
    }
    return ShapeEntity.ShapeArgs(d, unknownFields)
}

// --- ShapeEntity.RectArgs ---
private fun ShapeEntity.RectArgs.protoMergeImpl(other: Message?): ShapeEntity.RectArgs {
    if (other == null || other !is ShapeEntity.RectArgs) return this
    return copy(
        x = other.x ?: x, y = other.y ?: y,
        width = other.width ?: width, height = other.height ?: height,
        cornerRadius = other.cornerRadius ?: cornerRadius,
        unknownFields = unknownFields + other.unknownFields
    )
}

private fun ShapeEntity.RectArgs.Companion.decodeWithImpl(u: MessageDecoder): ShapeEntity.RectArgs {
    var x: Float? = null; var y: Float? = null
    var width: Float? = null; var height: Float? = null; var cornerRadius: Float? = null
    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> x = _fieldValue as Float
            2 -> y = _fieldValue as Float
            3 -> width = _fieldValue as Float
            4 -> height = _fieldValue as Float
            5 -> cornerRadius = _fieldValue as Float
        }
    }
    return ShapeEntity.RectArgs(x, y, width, height, cornerRadius, unknownFields)
}

// --- ShapeEntity.EllipseArgs ---
private fun ShapeEntity.EllipseArgs.protoMergeImpl(other: Message?): ShapeEntity.EllipseArgs {
    if (other == null || other !is ShapeEntity.EllipseArgs) return this
    return copy(
        x = other.x ?: x, y = other.y ?: y,
        radiusX = other.radiusX ?: radiusX, radiusY = other.radiusY ?: radiusY,
        unknownFields = unknownFields + other.unknownFields
    )
}

private fun ShapeEntity.EllipseArgs.Companion.decodeWithImpl(u: MessageDecoder): ShapeEntity.EllipseArgs {
    var x: Float? = null; var y: Float? = null
    var radiusX: Float? = null; var radiusY: Float? = null
    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> x = _fieldValue as Float
            2 -> y = _fieldValue as Float
            3 -> radiusX = _fieldValue as Float
            4 -> radiusY = _fieldValue as Float
        }
    }
    return ShapeEntity.EllipseArgs(x, y, radiusX, radiusY, unknownFields)
}

// --- ShapeEntity.RGBAColor ---
private fun ShapeEntity.RGBAColor.protoMergeImpl(other: Message?): ShapeEntity.RGBAColor {
    if (other == null || other !is ShapeEntity.RGBAColor) return this
    return copy(
        r = other.r ?: r, g = other.g ?: g, b = other.b ?: b, a = other.a ?: a,
        unknownFields = unknownFields + other.unknownFields
    )
}

private fun ShapeEntity.RGBAColor.Companion.decodeWithImpl(u: MessageDecoder): ShapeEntity.RGBAColor {
    var r: Float? = null; var g: Float? = null; var b: Float? = null; var a: Float? = null
    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> r = _fieldValue as Float
            2 -> g = _fieldValue as Float
            3 -> b = _fieldValue as Float
            4 -> a = _fieldValue as Float
        }
    }
    return ShapeEntity.RGBAColor(r, g, b, a, unknownFields)
}

// --- ShapeEntity.ShapeStyle ---
@Suppress("UNCHECKED_CAST")
private fun ShapeEntity.ShapeStyle.protoMergeImpl(other: Message?): ShapeEntity.ShapeStyle {
    if (other == null || other !is ShapeEntity.ShapeStyle) return this
    return copy(
        fill = other.fill?.let { fill?.plus(it) ?: it } ?: fill,
        stroke = other.stroke?.let { stroke?.plus(it) ?: it } ?: stroke,
        strokeWidth = other.strokeWidth ?: strokeWidth,
        lineCap = other.lineCap ?: lineCap,
        lineJoin = other.lineJoin ?: lineJoin,
        miterLimit = other.miterLimit ?: miterLimit,
        lineDashI = other.lineDashI ?: lineDashI,
        lineDashII = other.lineDashII ?: lineDashII,
        lineDashIII = other.lineDashIII ?: lineDashIII,
        unknownFields = unknownFields + other.unknownFields
    )
}

@Suppress("UNCHECKED_CAST")
private fun ShapeEntity.ShapeStyle.Companion.decodeWithImpl(u: MessageDecoder): ShapeEntity.ShapeStyle {
    var fill: ShapeEntity.RGBAColor? = null
    var stroke: ShapeEntity.RGBAColor? = null
    var strokeWidth: Float? = null
    var lineCap: ShapeEntity.ShapeStyle.LineCap? = null
    var lineJoin: ShapeEntity.ShapeStyle.LineJoin? = null
    var miterLimit: Float? = null
    var lineDashI: Float? = null
    var lineDashII: Float? = null
    var lineDashIII: Float? = null
    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> fill = _fieldValue as ShapeEntity.RGBAColor
            2 -> stroke = _fieldValue as ShapeEntity.RGBAColor
            3 -> strokeWidth = _fieldValue as Float
            4 -> lineCap = _fieldValue as ShapeEntity.ShapeStyle.LineCap
            5 -> lineJoin = _fieldValue as ShapeEntity.ShapeStyle.LineJoin
            6 -> miterLimit = _fieldValue as Float
            7 -> lineDashI = _fieldValue as Float
            8 -> lineDashII = _fieldValue as Float
            9 -> lineDashIII = _fieldValue as Float
        }
    }
    return ShapeEntity.ShapeStyle(fill, stroke, strokeWidth, lineCap, lineJoin, miterLimit, lineDashI, lineDashII, lineDashIII, unknownFields)
}

// --- ShapeEntity ---
@Suppress("UNCHECKED_CAST")
private fun ShapeEntity.protoMergeImpl(other: Message?): ShapeEntity {
    if (other == null || other !is ShapeEntity) return this
    return copy(
        type = other.type ?: type,
        shape = other.shape?.let { shape?.plus(it) ?: it } ?: shape,
        rect = other.rect?.let { rect?.plus(it) ?: it } ?: rect,
        ellipse = other.ellipse?.let { ellipse?.plus(it) ?: it } ?: ellipse,
        styles = other.styles?.let { styles?.plus(it) ?: it } ?: styles,
        transform = other.transform?.let { transform?.plus(it) ?: it } ?: transform,
        unknownFields = unknownFields + other.unknownFields
    )
}

@Suppress("UNCHECKED_CAST")
private fun ShapeEntity.Companion.decodeWithImpl(u: MessageDecoder): ShapeEntity {
    var type: ShapeEntity.ShapeType? = null
    var shape: ShapeEntity.ShapeArgs? = null
    var rect: ShapeEntity.RectArgs? = null
    var ellipse: ShapeEntity.EllipseArgs? = null
    var styles: ShapeEntity.ShapeStyle? = null
    var transform: Transform? = null
    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> type = _fieldValue as ShapeEntity.ShapeType
            2 -> shape = _fieldValue as ShapeEntity.ShapeArgs
            3 -> rect = _fieldValue as ShapeEntity.RectArgs
            4 -> ellipse = _fieldValue as ShapeEntity.EllipseArgs
            10 -> styles = _fieldValue as ShapeEntity.ShapeStyle
            11 -> transform = _fieldValue as Transform
        }
    }
    return ShapeEntity(type, shape, rect, ellipse, styles, transform, unknownFields)
}

// --- FrameEntity ---
@Suppress("UNCHECKED_CAST")
private fun FrameEntity.protoMergeImpl(other: Message?): FrameEntity {
    if (other == null || other !is FrameEntity) return this
    return copy(
        alpha = other.alpha ?: alpha,
        layout = other.layout?.let { layout?.plus(it) ?: it } ?: layout,
        transform = other.transform?.let { transform?.plus(it) ?: it } ?: transform,
        clipPath = other.clipPath ?: clipPath,
        shapes = shapes + other.shapes,
        unknownFields = unknownFields + other.unknownFields
    )
}

@Suppress("UNCHECKED_CAST")
private fun FrameEntity.Companion.decodeWithImpl(u: MessageDecoder): FrameEntity {
    var alpha: Float? = null
    var layout: Layout? = null
    var transform: Transform? = null
    var clipPath: String? = null
    var shapes: pbandk.ListWithSize.Builder<ShapeEntity>? = null
    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> alpha = _fieldValue as Float
            2 -> layout = _fieldValue as Layout
            3 -> transform = _fieldValue as Transform
            4 -> clipPath = _fieldValue as String
            5 -> shapes = (shapes ?: pbandk.ListWithSize.Builder()).apply { this += _fieldValue as kotlin.sequences.Sequence<ShapeEntity> }
        }
    }
    return FrameEntity(alpha, layout, transform, clipPath, pbandk.ListWithSize.Builder.fixed(shapes), unknownFields)
}

// --- AudioEntity ---
private fun AudioEntity.protoMergeImpl(other: Message?): AudioEntity {
    if (other == null || other !is AudioEntity) return this
    return copy(
        audioKey = other.audioKey ?: audioKey,
        startFrame = other.startFrame ?: startFrame,
        endFrame = other.endFrame ?: endFrame,
        startTime = other.startTime ?: startTime,
        totalTime = other.totalTime ?: totalTime,
        unknownFields = unknownFields + other.unknownFields
    )
}

private fun AudioEntity.Companion.decodeWithImpl(u: MessageDecoder): AudioEntity {
    var audioKey: String? = null
    var startFrame: Int? = null; var endFrame: Int? = null
    var startTime: Int? = null; var totalTime: Int? = null
    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> audioKey = _fieldValue as String
            2 -> startFrame = _fieldValue as Int
            3 -> endFrame = _fieldValue as Int
            4 -> startTime = _fieldValue as Int
            5 -> totalTime = _fieldValue as Int
        }
    }
    return AudioEntity(audioKey, startFrame, endFrame, startTime, totalTime, unknownFields)
}

// --- SpriteEntity ---
@Suppress("UNCHECKED_CAST")
private fun SpriteEntity.protoMergeImpl(other: Message?): SpriteEntity {
    if (other == null || other !is SpriteEntity) return this
    return copy(
        imageKey = other.imageKey ?: imageKey,
        frames = frames + other.frames,
        matteKey = other.matteKey ?: matteKey,
        unknownFields = unknownFields + other.unknownFields
    )
}

@Suppress("UNCHECKED_CAST")
private fun SpriteEntity.Companion.decodeWithImpl(u: MessageDecoder): SpriteEntity {
    var imageKey: String? = null
    var frames: pbandk.ListWithSize.Builder<FrameEntity>? = null
    var matteKey: String? = null
    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> imageKey = _fieldValue as String
            2 -> frames = (frames ?: pbandk.ListWithSize.Builder()).apply { this += _fieldValue as kotlin.sequences.Sequence<FrameEntity> }
            3 -> matteKey = _fieldValue as String
        }
    }
    return SpriteEntity(imageKey, pbandk.ListWithSize.Builder.fixed(frames), matteKey, unknownFields)
}

// --- MovieEntity ---
@Suppress("UNCHECKED_CAST")
private fun MovieEntity.protoMergeImpl(other: Message?): MovieEntity {
    if (other == null || other !is MovieEntity) return this
    return copy(
        version = other.version ?: version,
        params = other.params?.let { params?.plus(it) ?: it } ?: params,
        images = images + other.images,
        sprites = sprites + other.sprites,
        audios = audios + other.audios,
        unknownFields = unknownFields + other.unknownFields
    )
}

@Suppress("UNCHECKED_CAST")
private fun MovieEntity.Companion.decodeWithImpl(u: MessageDecoder): MovieEntity {
    var version: String? = null
    var params: MovieParams? = null
    var images: pbandk.MessageMap.Builder<String, ByteArr>? = null
    var sprites: pbandk.ListWithSize.Builder<SpriteEntity>? = null
    var audios: pbandk.ListWithSize.Builder<AudioEntity>? = null
    val unknownFields = u.readMessage(this) { _fieldNumber, _fieldValue ->
        when (_fieldNumber) {
            1 -> version = _fieldValue as String
            2 -> params = _fieldValue as MovieParams
            3 -> images = (images ?: pbandk.MessageMap.Builder()).apply { this.entries += _fieldValue as kotlin.sequences.Sequence<pbandk.MessageMap.Entry<String, ByteArr>> }
            4 -> sprites = (sprites ?: pbandk.ListWithSize.Builder()).apply { this += _fieldValue as kotlin.sequences.Sequence<SpriteEntity> }
            5 -> audios = (audios ?: pbandk.ListWithSize.Builder()).apply { this += _fieldValue as kotlin.sequences.Sequence<AudioEntity> }
        }
    }
    return MovieEntity(version, params, pbandk.MessageMap.Builder.fixed(images), pbandk.ListWithSize.Builder.fixed(sprites), pbandk.ListWithSize.Builder.fixed(audios), unknownFields)
}
