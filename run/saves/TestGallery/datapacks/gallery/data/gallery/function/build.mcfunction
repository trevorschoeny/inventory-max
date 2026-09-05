gamerule minecraft:advance_time false
time set day
fill -1 64 -2 5 72 6 minecraft:air
fill -1 63 -2 5 63 6 minecraft:red_concrete
setblock 2 64 0 minecraft:oak_sign[rotation=8]{is_waxed:1b,front_text:{messages:["#1", "Press P on a", "hotbar slot,", "then arrows to"]}}
fill -10 64 -2 -4 72 6 minecraft:air
fill -10 63 -2 -4 63 6 minecraft:lime_concrete
setblock -7 64 0 minecraft:oak_sign[rotation=8]{is_waxed:1b,front_text:{messages:["#2", "Take elytra and", "totem, fill the", "two new slots"]}}
setblock -7 64 2 minecraft:chest[facing=north]
item replace block -7 64 2 container.0 with minecraft:elytra 1
item replace block -7 64 2 container.1 with minecraft:totem_of_undying 1
fill -19 64 -2 -13 72 6 minecraft:air
fill -19 63 -2 -13 63 6 minecraft:light_blue_concrete
setblock -16 64 0 minecraft:oak_sign[rotation=8]{is_waxed:1b,front_text:{messages:["#3", "Open chest,", "press L on a", "slot"]}}
setblock -16 64 2 minecraft:chest[facing=north]
item replace block -16 64 2 container.0 with minecraft:cobblestone 32
item replace block -16 64 2 container.1 with minecraft:diamond 3
item replace block -16 64 2 container.2 with minecraft:iron_ingot 16
fill -28 64 -2 -22 72 6 minecraft:air
fill -28 63 -2 -22 63 6 minecraft:yellow_concrete
setblock -25 64 0 minecraft:oak_sign[rotation=8]{is_waxed:1b,front_text:{messages:["#4", "Stow pickaxe in", "inventory,", "throw XP"]}}
setblock -25 64 2 minecraft:chest[facing=north]
item replace block -25 64 2 container.0 with minecraft:diamond_pickaxe[minecraft:damage=1400,minecraft:enchantments={"minecraft:mending":1}] 1
item replace block -25 64 2 container.9 with minecraft:experience_bottle 64
setworldspawn 0 64 0
