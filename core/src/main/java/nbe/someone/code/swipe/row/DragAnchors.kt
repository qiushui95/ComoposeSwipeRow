package nbe.someone.code.swipe.row

public sealed class DragAnchors {
    public data object Start : DragAnchors()

    public data object Center : DragAnchors()

    public data object End : DragAnchors()
}
