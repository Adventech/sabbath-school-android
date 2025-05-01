/*
 * Copyright (c) 2025. Adventech <info@adventech.io>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ss.resource.components

import android.graphics.Bitmap
import coil.size.Size
import coil.transform.Transformation

internal class BottomHalfTransformation : Transformation {

    override val cacheKey: String = "Bottom40PercentTransformation"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val height = input.height
        val startY = (height * 0.6f).toInt() // Start at 60% height (shows bottom 40%)
        val bottomHeight = height - startY

        // Create the cropped bitmap
        val cropped = Bitmap.createBitmap(input, 0, startY, input.width, bottomHeight)

        // Recycle the original bitmap if it's mutable (to save memory)
        if (input.isMutable) {
            input.recycle()
        }

        return cropped
    }
}
