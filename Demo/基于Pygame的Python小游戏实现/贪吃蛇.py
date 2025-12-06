import pgzrun
import random

TILE_SIZE = 20          # 方块大小
WIDTH = 40 * TILE_SIZE  # 窗口宽度
HEIGHT = 30 * TILE_SIZE # 窗口高度
GAME_SPEED = 0.15       # 游戏速度(秒)
direction = 'up'        # 蛇移动方向
is_game_over = False    # 游戏是否结束
score = 0               # 游戏得分
snake = []              # 蛇身列表
obstacles = []          # 障碍物列表
OBSTACLE_COUNT = 8      # 障碍物数量


def init_game():
    """初始化游戏"""
    global snake, cookie, direction, is_game_over, score, obstacles

    direction = 'up'
    is_game_over = False
    score = 0
    snake.clear()
    obstacles.clear()

    snake_head = Actor('snake1')
    snake_head.x = WIDTH // 2
    snake_head.y = HEIGHT // 2
    snake.append(snake_head)

    for i in range(4):
        snake_body = Actor('snake1')
        snake_body.x = snake[i].x
        snake_body.y = snake[i].y + TILE_SIZE
        snake.append(snake_body)

    generate_obstacles()

    generate_food()


def generate_obstacles():
    """生成随机障碍物"""
    global obstacles

    for _ in range(OBSTACLE_COUNT):
        while True:
            obstacle = Actor('obstacle')
            obstacle.x = random.randint(2, WIDTH // TILE_SIZE - 3) * TILE_SIZE
            obstacle.y = random.randint(2, HEIGHT // TILE_SIZE - 3) * TILE_SIZE

            # 确保障碍物不会生成在蛇身上或离蛇太近
            obstacle_valid = True
            for segment in snake:
                distance_x = abs(obstacle.x - segment.x)
                distance_y = abs(obstacle.y - segment.y)
                if distance_x <= TILE_SIZE * 2 and distance_y <= TILE_SIZE * 2:
                    obstacle_valid = False
                    break

            # 确保障碍物之间不重叠
            if obstacle_valid:
                for existing_obstacle in obstacles:
                    if obstacle.x == existing_obstacle.x and obstacle.y == existing_obstacle.y:
                        obstacle_valid = False
                        break

            if obstacle_valid:
                obstacles.append(obstacle)
                break


def generate_food():
    """生成食物在随机位置"""
    global cookie
    while True:
        cookie = Actor('cookie')
        cookie.x = random.randint(1, WIDTH // TILE_SIZE - 1) * TILE_SIZE
        cookie.y = random.randint(1, HEIGHT // TILE_SIZE - 1) * TILE_SIZE

        # 确保食物不会生成在蛇身上
        food_on_snake = False
        for segment in snake:
            if cookie.x == segment.x and cookie.y == segment.y:
                food_on_snake = True
                break

        # 确保食物不会生成在障碍物上
        if not food_on_snake:
            for obstacle in obstacles:
                if cookie.x == obstacle.x and cookie.y == obstacle.y:
                    food_on_snake = True
                    break

        if not food_on_snake:
            break


def draw():
    """绘制游戏画面"""
    screen.clear()

    for obstacle in obstacles:
        obstacle.draw()

    for segment in snake:
        segment.draw()

    cookie.draw()

    screen.draw.text(f"得分: {score}", (10, 10),
                     fontsize=30, color='white', fontname='s')
    if is_game_over:
        screen.draw.text("游戏结束!", (WIDTH // 2 - 120, HEIGHT // 2 - 60),
                         fontsize=60, color='red', fontname='s')


def update():
    """处理键盘输入"""
    global direction

    if keyboard.left and direction != 'right':
        direction = 'left'
    elif keyboard.right and direction != 'left':
        direction = 'right'
    elif keyboard.up and direction != 'down':
        direction = 'up'
    elif keyboard.down and direction != 'up':
        direction = 'down'


def move_snake():
    """移动蛇身"""
    global direction, is_game_over, score

    if is_game_over:
        return

    old_head = snake[0]
    new_head = Actor('snake1')

    if direction == 'right':
        new_head.x = old_head.x + TILE_SIZE
        new_head.y = old_head.y
    elif direction == 'left':
        new_head.x = old_head.x - TILE_SIZE
        new_head.y = old_head.y
    elif direction == 'up':
        new_head.x = old_head.x
        new_head.y = old_head.y - TILE_SIZE
    elif direction == 'down':
        new_head.x = old_head.x
        new_head.y = old_head.y + TILE_SIZE

    if (new_head.x < 0 or new_head.x >= WIDTH or
            new_head.y < 0 or new_head.y >= HEIGHT):
        is_game_over = True
        return

    for segment in snake:
        if new_head.x == segment.x and new_head.y == segment.y:
            is_game_over = True
            return

    for obstacle in obstacles:
        if new_head.x == obstacle.x and new_head.y == obstacle.y:
            is_game_over = True
            return

    snake.insert(0, new_head)

    if new_head.x == cookie.x and new_head.y == cookie.y:
        score += 1
        generate_food()

        # 每吃5个食物增加一个新障碍物
        if score % 5 == 0 and len(obstacles) < 15:
            add_new_obstacle()

        # 吃到食物后蛇身长度增加，不删除尾部
    else:
        # 没吃到食物，删除尾部保持长度不变
        snake.pop()

    clock.schedule_unique(move_snake, GAME_SPEED)


def add_new_obstacle():
    """动态添加新障碍物"""
    attempts = 0
    while attempts < 50:  # 最多尝试50次
        obstacle = Actor('snake1')
        obstacle.x = random.randint(2, WIDTH // TILE_SIZE - 3) * TILE_SIZE
        obstacle.y = random.randint(2, HEIGHT // TILE_SIZE - 3) * TILE_SIZE

        # 检查位置是否有效
        position_valid = True

        # 不能在蛇身上
        for segment in snake:
            if obstacle.x == segment.x and obstacle.y == segment.y:
                position_valid = False
                break

        # 不能在食物位置
        if position_valid and obstacle.x == cookie.x and obstacle.y == cookie.y:
            position_valid = False

        # 不能与现有障碍物重叠
        if position_valid:
            for existing_obstacle in obstacles:
                if obstacle.x == existing_obstacle.x and obstacle.y == existing_obstacle.y:
                    position_valid = False
                    break

        if position_valid:
            obstacles.append(obstacle)
            break

        attempts += 1


init_game()
move_snake()

pgzrun.go()