package com.pokemod.rarecandy.tools;

import java.util.function.Consumer;

public record Command(String name, String description, Consumer<String[]> consumer) {}
