package ru.fierylynx.old43

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.ui.List
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable

class Styles {
    companion object {
        val labelBlackStyle = Label.LabelStyle().apply {
            font = Assets.font
            fontColor = Color.BLACK
        }
        val labelWhiteStyle = Label.LabelStyle().apply {
            font = Assets.font
            fontColor = Color.WHITE
        }
        val textButtonStyle = TextButton.TextButtonStyle().apply {
            font = Assets.font
            fontColor = Color.BLACK
        }
        val listSStyle = List.ListStyle().apply {
            font = Assets.font
            fontColorSelected = Color.RED
            fontColorUnselected = Color.BLACK
            selection = SpriteDrawable(Sprite(Texture("a.png")))
        }
        val scrollPaneStyle = ScrollPane.ScrollPaneStyle().apply {

        }
        val selectBoxStyle = SelectBox.SelectBoxStyle().apply {
            font = Assets.font
            fontColor = Color.BLACK
            scrollStyle = scrollPaneStyle
            listStyle = listSStyle
        }
    }
}