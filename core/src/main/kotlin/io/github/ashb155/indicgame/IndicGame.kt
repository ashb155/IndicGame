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
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

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

    private val shapeRenderer = ShapeRenderer()

    init {
        //static body
        world.body(type = BodyDef.BodyType.StaticBody) {
            position.set(0f, -5f)
            box(width = 20f, height = 1f)
        }

        world.body(type = BodyDef.BodyType.StaticBody) {
            position.set(25f, -3f)
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
        //clears screen to sunset orange
        clearScreen(0.9f, 0.4f, 0.2f, 1f)

        val maxSpeed = 8f
        val acceleration = 40f
        val vel = player.linearVelocity
        var isGrounded = false
        val feetPosition = Vector2(player.position.x, player.position.y - 1.1f)

        world.rayCast({ fixture, _, _, _ ->
            if (fixture.body.type == BodyDef.BodyType.StaticBody) {
                isGrounded = true
            }
            0f
        }, player.position, feetPosition)

        //movement logic
        if (Gdx.input.isKeyPressed(Input.Keys.D) && vel.x < maxSpeed) {
            player.applyForceToCenter(acceleration, 0f, true)
        } else if (Gdx.input.isKeyPressed(Input.Keys.A) && vel.x > -maxSpeed) {
            player.applyForceToCenter(-acceleration, 0f, true)
        } else {
            if (isGrounded) {
                player.setLinearVelocity(vel.x * 0.92f, vel.y)
            } else {
                player.setLinearVelocity(vel.x * 0.98f, vel.y)
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && isGrounded) {
            player.applyLinearImpulse(0f, 15f, player.worldCenter.x, player.worldCenter.y, true)
        }

        if (vel.y < 0) {
            player.gravityScale = 2.5f
        } else if (vel.y > 0 && !Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            player.gravityScale = 6.0f
        } else {
            player.gravityScale = 1.0f
        }

        camera.position.set(player.position.x, 0f, 0f)
        camera.update()

        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        val camX = camera.position.x

        //background
        shapeRenderer.color = Color(0.6f, 0.3f, 0.2f, 1f)
        val parallax1 = camX * 0.8f
        for (i in -3..5) {
            val offsetX = parallax1 + (i * 15f)
            shapeRenderer.rect(offsetX, -5f, 6f, 8f)
        }

        //foreground
        shapeRenderer.color = Color.DARK_GRAY
        shapeRenderer.rect(-10f, -5.5f, 20f, 1f)
        shapeRenderer.rect(15f, -3.5f, 20f, 1f)

        //player
        shapeRenderer.color = Color.FIREBRICK
        shapeRenderer.rect(player.position.x - 0.5f, player.position.y - 1f, 1f, 2f)

        shapeRenderer.end()

        //renders components
        debugRenderer.render(world, camera.combined)
        //world moves forward in time
        world.step(1/60f, 6, 2)
    }

    override fun dispose() {
        world.dispose()
        debugRenderer.dispose()
        shapeRenderer.dispose()
    }
}
