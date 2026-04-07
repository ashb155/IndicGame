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
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils

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
    private val batch = SpriteBatch()
    private val stoneTexture = createStoneTexture()

    //rough stone texture
    private fun createStoneTexture(): Texture {
        val pixmap = Pixmap(64, 64, Pixmap.Format.RGBA8888)
        for (x in 0 until 64) {
            for (y in 0 until 64) {
                val noise = MathUtils.random(0.15f, 0.25f)
                pixmap.setColor(noise + 0.1f, noise, noise - 0.05f, 1f)
                pixmap.drawPixel(x, y)
            }
        }
        val texture = Texture(pixmap)
        pixmap.dispose()
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
        return texture
    }

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

        val maxSpeed = 16f
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

        //respawn logic
        if (player.position.y < -15f) {
            player.setTransform(0f, 8f, 0f)
            player.setLinearVelocity(0f, 0f)
        }

        //enables transparency bleeding
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        val camX = camera.position.x

        //sky gradient
        val skyTop = Color(0.4f, 0.1f, 0.3f, 1f)
        val skyBottom = Color(1.0f, 0.6f, 0.1f, 1f)
        shapeRenderer.rect(camX - 20f, -10f, 40f, 30f, skyBottom, skyBottom, skyTop, skyTop)

        //sun
        shapeRenderer.color = Color(1f, 0.8f, 0.2f, 0.3f)
        shapeRenderer.circle(camX + 5f, 2f, 10f, 100)
        shapeRenderer.color = Color(1f, 0.9f, 0.4f, 1f)
        shapeRenderer.circle(camX + 5f, 2f, 7f, 100)

        //distant bg
        shapeRenderer.color = Color(0.75f, 0.35f, 0.2f, 1f)
        val parallax1 = camX * 0.8f
        for (i in -3..5) {
            val offsetX = parallax1 + (i * 15f)
            shapeRenderer.rect(offsetX, -5f, 6f, 4f) // Shorter base
            shapeRenderer.circle(offsetX + 3f, -1f, 3.2f, 50) // Dome
            shapeRenderer.rect(offsetX + 2.85f, 2f, 0.3f, 1.5f) // Spire
        }

        //midground bg
        shapeRenderer.color = Color(0.5f, 0.2f, 0.1f, 1f)
        val parallax2 = camX * 0.5f
        for (i in -4..6) {
            val offsetX = parallax2 + (i * 10f)
            shapeRenderer.rect(offsetX, -5f, 4f, 4f)
            shapeRenderer.rect(offsetX + 4f, -5f, 2f, 6f)
        }

        shapeRenderer.end()

        //foreground
        batch.projectionMatrix = camera.combined
        batch.begin()
        batch.draw(stoneTexture, -10f, -5.5f, 20f, 1f, 0f, 0f, 20f, 1f)
        batch.draw(stoneTexture, 15f, -3.5f, 20f, 1f, 0f, 0f, 20f, 1f)
        batch.end()

        //player
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color.FIREBRICK
        shapeRenderer.rect(player.position.x - 0.5f, player.position.y - 1f, 1f, 2f)
        shapeRenderer.end()

        //renders components
        //debugRenderer.render(world, camera.combined)
        //world moves forward in time
        world.step(1/60f, 6, 2)
    }

    override fun dispose() {
        world.dispose()
        debugRenderer.dispose()
        shapeRenderer.dispose()
        batch.dispose()
        stoneTexture.dispose()
    }
}
