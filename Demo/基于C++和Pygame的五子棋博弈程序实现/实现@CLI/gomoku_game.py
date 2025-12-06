import pygame
import subprocess

# 常量
BOARD_SIZE = 15
CELL_SIZE = 40
SCREEN_SIZE = CELL_SIZE * BOARD_SIZE
BLACK, WHITE, EMPTY = 1, 2, 0
BG_COLOR = (245, 222, 179)
LINE_COLOR = (0, 0, 0)
STONE_COLORS = {BLACK: (0, 0, 0), WHITE: (255, 255, 255)}
FONT_COLOR = (255, 0, 0)

# 初始三手交换阶段
PHASE_THREE_HAND = 1
PHASE_FIVE_N_HAND = 2
PHASE_NORMAL = 3

pygame.init()
screen = pygame.display.set_mode((SCREEN_SIZE, SCREEN_SIZE))
pygame.display.set_caption("五子棋（含禁手 + 三手交换 + 五手N打）")
font = pygame.font.SysFont("SimHei", 24)

board = [[0 for _ in range(BOARD_SIZE)] for _ in range(BOARD_SIZE)]
history = []
phase = PHASE_THREE_HAND
turn = BLACK
swap_done = False

def draw_board():
    screen.fill(BG_COLOR)
    for i in range(BOARD_SIZE):
        pygame.draw.line(screen, LINE_COLOR, (CELL_SIZE // 2, CELL_SIZE // 2 + i * CELL_SIZE),
                         (SCREEN_SIZE - CELL_SIZE // 2, CELL_SIZE // 2 + i * CELL_SIZE))
        pygame.draw.line(screen, LINE_COLOR, (CELL_SIZE // 2 + i * CELL_SIZE, CELL_SIZE // 2),
                         (CELL_SIZE // 2 + i * CELL_SIZE, SCREEN_SIZE - CELL_SIZE // 2))

    for y in range(BOARD_SIZE):
        for x in range(BOARD_SIZE):
            if board[y][x] != EMPTY:
                pygame.draw.circle(screen, STONE_COLORS[board[y][x]],
                                   (x * CELL_SIZE + CELL_SIZE // 2, y * CELL_SIZE + CELL_SIZE // 2), CELL_SIZE // 2 - 2)

    text = font.render(f"当前阶段: {'三手交换' if phase==1 else ('五手N打' if phase==2 else '正常')}", True, FONT_COLOR)
    screen.blit(text, (10, SCREEN_SIZE - 30))

def is_forbidden_move(x, y):
    # TODO：添加具体禁手判定，如长连、三三、四四等
    return False if turn != BLACK else False

def get_ai_move():
    board_str = '\n'.join(','.join(str(cell) for cell in row) for row in board)
    try:
        result = subprocess.run(
            ["./backend.exe"], input=board_str.encode(), capture_output=True, check=True
        )
        x, y = map(int, result.stdout.decode().strip().split())
        return x, y
    except Exception as e:
        print("AI调用错误：", e)
        return -1, -1

def handle_click(pos):
    global turn, phase, swap_done
    global board
    x, y = pos[0] // CELL_SIZE, pos[1] // CELL_SIZE
    if board[y][x] != EMPTY or is_forbidden_move(x, y):
        return

    board[y][x] = turn
    history.append((x, y, turn))

    # 处理规则阶段
    if phase == PHASE_THREE_HAND:
        if len(history) == 3:
            print("白方选择是否交换？(控制台输入 yes/no): ", end="")
            if input().strip().lower() == "yes":
                turn = BLACK  # 白变黑
            phase = PHASE_FIVE_N_HAND
    elif phase == PHASE_FIVE_N_HAND and len(history) == 5:
        print("黑方选择一手给白方开局 (0~4): ", end="")
        idx = int(input().strip())
        x, y, _ = history[idx]
        board = [[EMPTY]*BOARD_SIZE for _ in range(BOARD_SIZE)]
        board[y][x] = WHITE  # 白棋执子
        history.clear()
        history.append((x, y, WHITE))
        turn = BLACK
        phase = PHASE_NORMAL
        return

    # 进入 AI 回合
    turn = WHITE if turn == BLACK else BLACK
    if turn == WHITE and phase == PHASE_NORMAL:
        x_ai, y_ai = get_ai_move()
        if 0 <= x_ai < BOARD_SIZE and 0 <= y_ai < BOARD_SIZE:
            board[y_ai][x_ai] = WHITE
            history.append((x_ai, y_ai, WHITE))
            turn = BLACK

# 主循环
running = True
while running:
    draw_board()
    pygame.display.flip()

    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            running = False
        elif event.type == pygame.MOUSEBUTTONDOWN and turn == BLACK:
            handle_click(pygame.mouse.get_pos())

pygame.quit()
