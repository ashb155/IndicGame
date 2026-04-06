package io.github.ashb155.indicgame
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import com.badlogic.gdx.physics.box2d.*
import ktx.box2d.*
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input

class IndicGame : KtxGame<KtxScreen>() {
    override fun create() {
        addScreen(FirstScreen())
        setScreen<FirstScreen>()
    }
}

class FirstScreen : KtxScreen {
    //creates world with gravity
    private val world = World(Vector2(0f, -9.8f), true)

    //debug renderer for world
    private val debugRenderer = Box2DDebugRenderer()
    //Camera
    private val camera = OrthographicCamera(32f, 18f)
    private val player: Body

    init {
        //static body
        world.body(type = BodyDef.BodyType.StaticBody) {
            position.set(0f, -5f)
            box(width = 20f, height = 1f)
        }

        //dynamic body
        player = world.body(type = BodyDef.BodyType.DynamicBody) {
            position.set(0f, 8f)
            box(width = 1f, height = 2f) {
                friction = 0.4f
                restitution = 0.1f
            }
        }
    }

    override fun render(delta: Float) {
        //clears screen to dark stealthy grey
        clearScreen(0.1f, 0.1f, 0.1f, 1f)

        val speed = 5f
        val currentVelocity = player.linearVelocity

        //movement logic
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            player.setLinearVelocity(-speed, currentVelocity.y)
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            player.setLinearVelocity(speed, currentVelocity.y)
        } else {
            player.setLinearVelocity(0f, currentVelocity.y)
        }

        //renders components
        debugRenderer.render(world, camera.combined)
        //world moves forward in time
        world.step(1/60f, 6, 2)
    }

    override fun dispose() {
        world.dispose()
        debugRenderer.dispose()
    }
}
