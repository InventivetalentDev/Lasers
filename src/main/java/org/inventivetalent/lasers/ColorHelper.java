/*
 * Copyright 2015-2016 inventivetalent. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and contributors and should not be interpreted as representing official policies,
 *  either expressed or implied, of anybody else.
 */

package org.inventivetalent.lasers;

import org.bukkit.Color;
import org.bukkit.DyeColor;

public class ColorHelper {

	private final Lasers plugin;

	public ColorHelper(Lasers lasers) {
		this.plugin = lasers;
	}

	public int colorToSignal(Color c) {
		if (c == null) { return 0; }

		DyeColor col = DyeColor.RED;

		for (int i = 0; i < DyeColor.values().length; i++) {
			DyeColor dc = DyeColor.values()[i];
			Color color = dc.getColor();

			int tol = this.plugin.receiverSignalTolerance;

			if (Math.abs(c.getRed() - color.getRed()) < tol) {
				if (Math.abs(c.getGreen() - color.getGreen()) < tol) {
					if (Math.abs(c.getBlue() - color.getBlue()) < tol) {
						col = dc;
					}
				}
			}
		}

		//TODO: get rid of data
		return DyeColor.values().length - (col.getWoolData() + 1);
	}

}
